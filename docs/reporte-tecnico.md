# Reporte Técnico — App de Mensajería "LightChat"

**Materia:** Programación Avanzada II
**Tipo de proyecto:** Aplicación de mensajería cliente-servidor en Java
**Fecha del reporte:** 2026-06-07

---

## 1. Resumen ejecutivo

LightChat es una aplicación de mensajería de escritorio construida con una
arquitectura **cliente-servidor clásica sobre sockets TCP**. El sistema se divide
en dos programas Java independientes:

- **ChatClient** — Aplicación de escritorio con interfaz gráfica **Swing**. Es lo
  que ve y usa el usuario final (login, registro, lista de usuarios, chats,
  grupos, solicitudes de amistad).
- **ChatServer** — Proceso servidor sin interfaz gráfica que escucha conexiones,
  procesa peticiones y habla con la base de datos **MySQL** (alojada en
  Aiven Cloud). Se despliega en una VM de Azure como servicio `systemd`.

La comunicación entre ambos se hace intercambiando **mensajes JSON** (serializados
con la librería **Jackson**), donde cada mensaje termina con un salto de línea
(`\n`) que actúa como delimitador de trama.

```
┌────────────────────┐        TCP / JSON + '\n'        ┌────────────────────┐        JDBC        ┌──────────────┐
│     ChatClient     │  ◄──────────────────────────►   │     ChatServer     │  ◄──────────────►  │  MySQL (Aiven)│
│  (Swing / Jackson) │   {"type":"LOGIN", ...}\n        │ (sockets + Jackson)│   PreparedStmt     │   defaultdb   │
└────────────────────┘                                 └────────────────────┘                    └──────────────┘
```

---

## 2. Estructura del repositorio

```
App-de-Mensajeria-PA-II/
├── ChatClient/              # Proyecto NetBeans/Ant — cliente Swing
│   ├── src/
│   │   ├── chatclient/      # main (ChatClient.java)
│   │   ├── view/            # Ventanas Swing (LoginView, ChatView, ...)
│   │   ├── controller/      # Stubs vacíos (lógica vive en las vistas)
│   │   ├── model/           # Modelos de dominio + dbrequest/ (DTOs salientes)
│   │   ├── socket/          # ClientSocket (Singleton de red)
│   │   └── database/        # DatabaseService (vacío, sin uso real)
│   └── .env                 # CHAT_HOST / CHAT_PORT
├── ChatServer/              # Proyecto NetBeans/Ant — servidor
│   ├── src/
│   │   ├── chatserver/      # main (ChatServer.java)
│   │   ├── server/          # JavaServer + ClientThread
│   │   ├── dao/             # Acceso a datos (UserDAO, MessageDAO, ...)
│   │   ├── database/        # DatabaseConnection (JDBC)
│   │   └── model/           # Modelos espejo del cliente
│   ├── .env                 # DB_URL / DB_USER / DB_PASSWORD
│   └── deploy.sh            # git pull + ant jar + systemctl restart
├── lib/                     # Jackson (core/databind/annotations) + MySQL connector
└── docs/                    # Documentación (diagramas, este reporte)
```

> **Nota:** Las carpetas `backend/` y `frontend/` en la raíz están vacías
> (estructura heredada inicial sin uso).

---

## 3. Stack tecnológico

| Componente            | Tecnología                                            |
|-----------------------|-------------------------------------------------------|
| Lenguaje              | Java (plataforma JDK 21 en NetBeans)                  |
| Build                 | Apache Ant (`build.xml`, proyecto NetBeans)           |
| Interfaz gráfica      | Java Swing (`JFrame`, `JOptionPane`, etc.)            |
| Transporte            | Sockets TCP (`java.net.Socket` / `ServerSocket`)      |
| Serialización         | Jackson 2.9.9 (`ObjectMapper`, `JsonNode`)            |
| Base de datos         | MySQL (Aiven Cloud), acceso vía JDBC                  |
| Driver BD             | `mysql-connector-j-9.7.0`                             |
| Despliegue servidor   | VM Azure + servicio `systemd` (`chatserver`)          |
| Configuración         | Archivos `.env` (no versionados)                      |

---

## 4. Arquitectura general

### 4.1 Patrón de comunicación

El sistema usa un **protocolo de aplicación propio sobre TCP**, orientado a líneas:

1. Cada petición/respuesta es un objeto **JSON serializado a una sola línea**.
2. Se agrega `\n` al final como **terminador de trama**.
3. El receptor usa `BufferedReader.readLine()`, que se bloquea hasta recibir la
   línea completa — esto garantiza que el JSON llega entero antes de procesarlo.
4. El campo discriminador es **`type`** (`LOGIN`, `SIGNUP`,
   `RECOVER_PASSWORD`, ...). Tanto cliente como servidor hacen *dispatch* según
   ese campo.

### 4.2 Concurrencia en el servidor

El servidor sigue el modelo clásico **"un hilo por cliente"**:

- `JavaServer.beginServer()` levanta un hilo que ejecuta el bucle
  `accept()` sobre un `ServerSocket` en el puerto **1235**.
- Por cada conexión entrante se crea un `ClientThread` (un `Runnable`), se le
  asigna un **id incremental** (`AtomicInteger`) y se guarda en un
  `ConcurrentHashMap<Integer, ClientThread>` de clientes activos.
- Cada `ClientThread` corre en su propio hilo y atiende su socket de forma
  independiente, leyendo líneas en bucle hasta que el cliente se desconecta.
- Al desconectarse (`readLine()` devuelve `null`), el `finally` llama a
  `removeClient(id)`, que lo quita del mapa.

### 4.3 Concurrencia en el cliente

- `ClientSocket` es un **Singleton** (constructor privado +
  `getInstance()` sincronizado) que centraliza toda la red del cliente.
- La escucha del servidor corre en un **hilo secundario** (`listen()`), para no
  bloquear la interfaz gráfica.
- Como Swing **no es thread-safe**, cuando llega un mensaje del servidor se
  reenvía a la vista activa mediante `SwingUtilities.invokeLater(...)`, que
  ejecuta el callback en el *Event Dispatch Thread* de Swing.
- La vista activa se suscribe pasando dos `Consumer<String>`:
  - `setStatusListener(...)` → recibe cambios de estado de conexión.
  - `setMessageListener(...)` → recibe el JSON crudo del servidor.

---

## 5. ChatClient (cliente) en detalle

### 5.1 Arranque

`ChatClient.main()` simplemente crea y muestra la `LoginView`. A partir de ahí la
navegación es **vista a vista**: cada ventana, al abrir la siguiente, hace
`dispose()` de sí misma.

### 5.2 ClientSocket — el corazón de la red

Archivo: [ChatClient/src/socket/ClientSocket.java](../ChatClient/src/socket/ClientSocket.java)

Responsabilidades:

- **Cargar configuración** desde `.env` (`CHAT_HOST`, `CHAT_PORT`), con *fallback*
  a variables de entorno del sistema y, por último, a `127.0.0.1:1235`.
- **Conectar con reintentos** (`tryConnect()`): intenta conectar en un bucle con
  espera de 2 s entre intentos, hasta lograrlo.
- **Escuchar** (`listen()`): lee líneas del servidor y las reenvía a la vista
  activa vía `invokeLater`.
- **Enviar** (`sendText()`): añade `\n` al JSON y lo escribe en el socket en UTF-8.
- **Gestionar el estado**: notifica "Conectado" / "Desconectado" /
  "Intento N fallido…" al `statusListener`.

### 5.3 Vistas (Swing)

Cada vista es un `JFrame` que concentra **tanto la UI como la lógica** de su
pantalla (en este proyecto los `controller/` son stubs vacíos; el patrón MVC está
esbozado pero no implementado — la lógica vive en las vistas).

| Vista                  | Función                                                |
|------------------------|--------------------------------------------------------|
| `LoginView`            | Inicio de sesión. Tras 3 errores abre recuperación.    |
| `SignUpView`           | Registro de nuevos usuarios.                           |
| `RecoverPasswordView`  | Cambio/recuperación de contraseña.                     |
| `UsersListView`        | Lista de usuarios/amigos; punto de entrada post-login. |
| `ChatView`             | Chat directo 1 a 1.                                    |
| `GroupsView`           | Lista de grupos del usuario.                           |
| `GroupChatView`        | Chat dentro de un grupo.                               |
| `NewGroupView`         | Creación de grupos.                                    |
| `PersonsInGroup`       | Miembros de un grupo.                                  |
| `FriendsRequestView`   | Solicitudes de amistad recibidas.                      |

### 5.4 Modelos y DTOs

- `model/` contiene los modelos de dominio: `User`, `Message`, `Chat`, `Group`,
  `UserGroup`, `FriendRequest`, `GroupInvitation`, y la jerarquía `Request`
  (clase abstracta con el enum `RequestStatus { APPROVED, PENDING, DENIED }`).
- `model/dbrequest/` contiene los **DTOs salientes** que el cliente serializa con
  Jackson y manda al servidor. Cada uno fija su `type`:
  - `LoginRequest` → `type = "LOGIN"`
  - `SignUpRequest` → `type = "SIGNUP"` (estado por defecto `disconnected`)
  - `RecoverPasswordRequest` → `type = "RECOVER_PASSWORD"`

  Todos tienen constructor vacío porque **Jackson lo requiere** para la
  (de)serialización.

---

## 6. ChatServer (servidor) en detalle

### 6.1 Arranque

`ChatServer.main()` instancia `JavaServer` y llama `beginServer()`. (El `main`
también contiene código de prueba/diagnóstico que lista usuarios y mensajes de la
BD por consola.)

### 6.2 JavaServer

Archivo: [ChatServer/src/server/JavaServer.java](../ChatServer/src/server/JavaServer.java)

- Puerto fijo `PORT = 1235`.
- Bucle `accept()` en hilo dedicado.
- Registro de clientes activos en `ConcurrentHashMap`.
- `writeConsole()` para logging por consola (sincronizado).

### 6.3 ClientThread — procesamiento de peticiones

Archivo: [ChatServer/src/server/ClientThread.java](../ChatServer/src/server/ClientThread.java)

Por cada cliente:

1. Abre `BufferedReader` (entrada) y `PrintWriter` (salida, *autoflush*), ambos en
   UTF-8.
2. Bucle `while (readLine() != null)` — lee cada trama JSON.
3. Parsea con `mapper.readTree()` y lee el campo `type`.
4. **Dispatch por tipo:**

   | `type`             | Acción                                                                 | Respuesta                                            |
   |--------------------|------------------------------------------------------------------------|------------------------------------------------------|
   | `LOGIN`            | `UserDAO.getUserByEmail()` y compara contraseña.                       | `LOGIN_SUCCESS` o `LOGIN_ERROR`                      |
   | `SIGNUP`           | `UserDAO.insertUser()`.                                                | `SIGNUP_SUCCESS`                                     |
   | `RECOVER_PASSWORD` | `UserDAO.changePassword()`.                                            | `RECOVER_PASSWORD_SUCCESS` o `RECOVER_PASSWROD_ERROR`|

5. Cualquier JSON inválido se captura y se loguea sin tirar el hilo.

> **Observación técnica:** las respuestas se construyen como **strings JSON
> escritos a mano** con `out.println(...)`, no serializando objetos con Jackson
> (el servidor usa Jackson solo para *leer*). El tipo `RECOVER_PASSWROD_ERROR`
> tiene un *typo* que debe coincidir entre cliente y servidor para funcionar.

### 6.4 Capa de datos (DAO)

Todos los DAO **heredan de `DatabaseConnection`**, que en su constructor abre la
conexión JDBC leyendo `DB_URL`/`DB_USER`/`DB_PASSWORD` del `.env`. Se usan
`PreparedStatement` en todas partes (protege contra inyección SQL).

| DAO                      | Responsabilidad                                                                 |
|--------------------------|---------------------------------------------------------------------------------|
| `UserDAO`                | CRUD de usuarios: insertar, listar, buscar por id/email, cambiar contraseña.    |
| `MessageDAO`             | Insertar mensajes y leerlos por conversación.                                   |
| `ConversationDAO`        | Crear conversaciones directas (`TEMP`/`FRIEND`) y de grupo (`GROUP`); buscar conversación entre 2 usuarios; promover `TEMP`→`FRIEND`; borrar conversación de forma transaccional. |
| `ConversationMemberDAO`  | Tabla pivote usuario↔conversación: agregar/quitar miembros, listar miembros, contar, verificar membresía. |
| `FriendshipDAO`          | Solicitudes y amistades: enviar, aceptar, denegar, listar amigos/solicitudes pendientes, verificar amistad. |

**Modelo conceptual de conversaciones:**

- `TEMP` — conversación temporal entre usuarios que aún no son amigos.
- `FRIEND` — conversación entre amigos confirmados (se promueve desde `TEMP` al
  aceptar la solicitud, **sin perder el historial**).
- `GROUP` — conversación ligada a un grupo (`id_grupo`).

> **Nota importante:** `UserDAO`/`MessageDAO` usan nombres de tabla/columna en
> **inglés** (`users`, `id_user`, `messages`), mientras que `ConversationDAO`,
> `ConversationMemberDAO` y `FriendshipDAO` usan nombres en **español**
> (`Usuarios`, `Conversaciones`, `amistades`, `id_usuario`). Esto sugiere dos
> generaciones de esquema. El flujo de chat/grupos/amistades depende del esquema
> en español, que debe existir en la BD para que esas funciones operen.

---

## 7. Flujos principales (paso a paso)

### 7.1 Conexión inicial

1. El usuario abre la app → `LoginView`.
2. La vista obtiene el Singleton `ClientSocket.getInstance()`, registra sus
   listeners y llama `tryConnect()`.
3. `ClientSocket` lanza un hilo que abre el `Socket(host, port)`.
4. El servidor acepta, crea `ClientThread`, lo registra y arranca su hilo.
5. El cliente entra en `listen()` (bloqueado en `readLine()` esperando datos).

### 7.2 Login

1. Usuario hace clic en "Log in" → la vista valida campos no vacíos.
2. Crea `LoginRequest(email, pass)` → `ObjectMapper.writeValueAsString(...)`.
3. `sendText(json)` → llega al servidor como `{"type":"LOGIN",...}\n`.
4. `ClientThread` consulta `UserDAO.getUserByEmail()` y compara contraseña.
5. Responde `LOGIN_SUCCESS` o `LOGIN_ERROR`.
6. El cliente recibe el JSON en su `messageListener`; si es éxito abre
   `UsersListView` y cierra el login; si falla muestra `JOptionPane` y, tras 3
   errores, abre `RecoverPasswordView`.

### 7.3 Registro / Recuperación de contraseña

Mismo patrón: la vista construye el DTO correspondiente, lo serializa, lo envía y
reacciona a la respuesta (`SIGNUP_SUCCESS`, `RECOVER_PASSWORD_SUCCESS`, etc.).

### 7.4 Desconexión

- Si se pierde la conexión, `readLine()` devuelve `null` y lanza excepción; el
  cliente cierra el socket y notifica "Desconectado".
- En el servidor, el `finally` de `ClientThread` llama `removeClient(id)` para
  quitarlo del mapa de clientes activos.

> Los diagramas de secuencia detallados de estos flujos están en
> [docs/diagrama-secuencia.md](./diagrama-secuencia.md).

---

## 8. Configuración y despliegue

### 8.1 Archivos `.env`

**ChatClient/.env**
```
CHAT_HOST=20.163.58.45   # IP de la VM de Azure (o 127.0.0.1 para pruebas locales)
CHAT_PORT=1235
```

**ChatServer/.env**
```
DB_URL=jdbc:mysql://...aivencloud.com:13866/defaultdb
DB_USER=avnadmin
DB_PASSWORD=********
```

Ambos archivos se cargan con `Properties.load()` y **no se versionan** (van en
`.gitignore`).

### 8.2 Despliegue del servidor (`deploy.sh`)

El script [ChatServer/deploy.sh](../ChatServer/deploy.sh) automatiza la
actualización en la VM de Azure:

1. `git pull` para traer los últimos cambios.
2. Recompila el `.jar` con `ant` (autodetectando la ruta del JDK, que NetBeans
   espera bajo el nombre de plataforma `JDK_21`).
3. `sudo systemctl restart chatserver` para reiniciar el servicio.
4. Muestra el estado del servicio.

### 8.3 Compilación local

Ambos son proyectos **NetBeans + Ant**. Se compilan con `ant jar` (o desde
NetBeans) y dependen de los `.jar` de la carpeta `lib/` (Jackson + MySQL
connector).

---

## 9. Observaciones técnicas y áreas de mejora

Hallazgos relevantes detectados durante la revisión del código:

1. **Seguridad — contraseñas en texto plano.** Las contraseñas se almacenan y
   comparan sin *hashing* (`retrievedUser.getPassword().equals(passReq)`). Debería
   usarse al menos BCrypt/Argon2.
2. **Seguridad — credenciales en el repo.** El `.env` del servidor con la
   contraseña real de la BD parece estar presente; conviene verificar que esté
   ignorado y rotar la credencial si se filtró.
3. **Respuestas JSON a mano.** El servidor escribe strings JSON manualmente en
   lugar de serializar con Jackson; es propenso a *typos* (`RECOVER_PASSWROD_ERROR`).
4. **MVC incompleto.** Los `controller/` son stubs vacíos; la lógica está en las
   vistas. Mover lógica de red/negocio a los controladores mejoraría la
   mantenibilidad.
5. **Funcionalidad de chat aún no cableada en el protocolo.** Existen DAOs
   completos para conversaciones, mensajes, grupos y amistades, pero el
   `ClientThread` solo maneja `LOGIN`, `SIGNUP` y `RECOVER_PASSWORD`. El envío de
   mensajes en tiempo real y el *broadcast* a otros clientes activos todavía no
   están implementados en el dispatcher del servidor.
6. **Inconsistencia de esquema (inglés vs. español).** Conviene unificar los
   nombres de tablas/columnas para evitar fallos silenciosos en runtime.
7. **Una conexión JDBC por DAO.** Cada `new XxxDAO()` abre una conexión nueva; un
   *pool* de conexiones sería más eficiente y robusto.

---

## 10. Conclusión

LightChat implementa de forma sólida los **fundamentos de una arquitectura
cliente-servidor concurrente**: sockets TCP, un protocolo JSON orientado a líneas,
un servidor multihilo con un hilo por cliente, un cliente Swing con red
desacoplada mediante un Singleton y callbacks seguros para el hilo de UI, y una
capa de persistencia DAO sobre MySQL con sentencias preparadas.

La base de autenticación (login, registro, recuperación) está **funcional de
extremo a extremo**, y la capa de datos para chats, grupos y amistades ya está
construida. El siguiente paso natural del proyecto es **cablear esos DAOs al
dispatcher del servidor** para habilitar el envío de mensajes y la entrega en
tiempo real entre clientes conectados, junto con las mejoras de seguridad
señaladas (hashing de contraseñas y gestión de credenciales).
