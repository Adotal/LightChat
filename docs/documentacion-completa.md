# Documentación Completa — App de Mensajería "LightChat"

**Materia:** Programación Avanzada II
**Tipo de proyecto:** Aplicación de mensajería cliente-servidor en Java
**Propósito de este documento:** referencia técnica + guía de defensa oral. Refleja el
estado **actual** del código (chat directo, grupos y amistades ya funcionan de extremo a
extremo). Para el contexto histórico ver también [reporte-tecnico.md](./reporte-tecnico.md)
y [diagrama-secuencia.md](./diagrama-secuencia.md).

> Nota: el `reporte-tecnico.md` describe una etapa anterior donde el servidor solo
> manejaba `LOGIN`/`SIGNUP`/`RECOVER_PASSWORD` y el esquema mezclaba inglés/español.
> **Este documento es la fuente actualizada:** el dispatcher del servidor ya maneja chat,
> grupos y amistades, y el esquema canónico (`ChatServer/schema.sql`) está unificado en
> inglés.

---

## Índice

1. [Visión general y stack](#1-visión-general-y-stack)
2. [Arquitectura del servidor](#2-arquitectura-del-servidor)
3. [Arquitectura del cliente](#3-arquitectura-del-cliente)
4. [Protocolo de comunicación](#4-protocolo-de-comunicación)
5. [Base de datos y capa DAO](#5-base-de-datos-y-capa-dao)
6. [Funcionalidades clave (flujos paso a paso)](#6-funcionalidades-clave-flujos-paso-a-paso)
7. [Concurrencia y threading](#7-concurrencia-y-threading)
8. [Configuración y despliegue](#8-configuración-y-despliegue)
9. [Preguntas y respuestas tipo examen](#9-preguntas-y-respuestas-tipo-examen)

---

## 1. Visión general y stack

LightChat es una app de mensajería de escritorio con **arquitectura cliente-servidor sobre
sockets TCP**. Dos programas Java independientes:

- **ChatClient** — interfaz gráfica **Swing**: login, registro, lista de usuarios, chat
  directo (efímero o persistente), grupos y solicitudes de amistad.
- **ChatServer** — proceso servidor (sin GUI salvo un panel de administración Swing) que
  escucha conexiones, procesa peticiones y persiste en **MySQL** vía JDBC.

La comunicación intercambia **mensajes JSON** (serializados con **Jackson**) donde cada
mensaje termina en un salto de línea (`\n`) que actúa como delimitador de trama.

```
┌────────────────────┐     TCP / JSON + '\n'      ┌────────────────────┐      JDBC      ┌──────────────┐
│     ChatClient     │  ◄────────────────────►    │     ChatServer     │  ◄──────────►  │  MySQL/Aiven │
│  (Swing + Jackson) │   {"type":"LOGIN",...}\n   │ (sockets + Jackson)│  PreparedStmt  │   defaultdb  │
└────────────────────┘                            └────────────────────┘                └──────────────┘
```

### Stack tecnológico

| Componente            | Tecnología                                            |
|-----------------------|-------------------------------------------------------|
| Lenguaje              | Java (JDK 21)                                         |
| Build                 | Apache Ant (proyecto NetBeans, `build.xml`)           |
| Interfaz gráfica      | Java Swing (Look & Feel Nimbus, tema oscuro)          |
| Transporte            | Sockets TCP (`java.net.Socket` / `ServerSocket`)      |
| Serialización         | Jackson 2.9.9 (`ObjectMapper`, `JsonNode`)            |
| Base de datos         | MySQL 8 (Aiven Cloud), acceso vía JDBC                |
| Driver BD             | `mysql-connector-j-9.7.0`                             |
| Despliegue servidor   | VM Azure + servicio `systemd` (`chatserver`)          |
| Configuración         | Archivos `.env` (no versionados)                      |

### Diagrama de capas (cliente)

```
┌──────────────────────────────────────────────────────┐
│ Vistas (Swing): LoginView, UsersListView, ChatView,  │  ← UI / EDT
│                 GroupChatView, ...                   │
├──────────────────────────────────────────────────────┤
│ Controllers: LoginController, UsersListController,   │  ← lógica de negocio
│              ChatController, GroupsController        │
├──────────────────────────────────────────────────────┤
│ ServerDispatcher: routing por "type" → handlers      │  ← des-multiplexa + invokeLater
├──────────────────────────────────────────────────────┤
│ ClientSocket (Singleton): TCP + hilo de escucha      │  ← red
├──────────────────────────────────────────────────────┤
│ Modelos: User, Message, Chat, Group, SessionManager  │  ← datos
└──────────────────────────────────────────────────────┘
```

---

## 2. Arquitectura del servidor

### 2.1 Punto de entrada

[ChatServer/src/chatserver/ChatServer.java](../ChatServer/src/chatserver/ChatServer.java)

```java
public static void main(String[] args) {
    JavaServer server = new JavaServer();
    // El panel admin se suscribe a eventos ANTES de arrancar el bucle
    SwingUtilities.invokeLater(() -> new AdminView(server).setVisible(true));
    server.beginServer();
    server.logEvent("SERVER_START", "Servidor iniciado.");
}
```

### 2.2 JavaServer — gestor de conexiones

[ChatServer/src/server/JavaServer.java](../ChatServer/src/server/JavaServer.java)

- Puerto fijo `PORT = 1235`.
- Bucle `accept()` en un hilo dedicado: por cada conexión crea un `ClientThread`, le asigna
  un id incremental (`AtomicInteger`) y lo guarda en un
  `ConcurrentHashMap<Integer, ClientThread>` de clientes activos.

```java
public void beginServer() {
    new Thread(() -> {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            while (!ss.isClosed()) {
                Socket clientSocket = ss.accept();
                int newId = idCount.incrementAndGet();
                ClientThread newClient = new ClientThread(clientSocket, newId, this);
                activeClients.put(newId, newClient);
                new Thread(newClient).start();
            }
        } catch (Exception ex) { writeConsole("Error en servidor: " + ex.getMessage()); }
    }).start();
}
```

Métodos de apoyo importantes:

- `findClientByUserEmail(String email)` — localiza el `ClientThread` de un usuario conectado
  (clave para **entregar mensajes en vivo**).
- `broadcastUserStatus()` — recorre todos los clientes activos y les manda la lista de
  usuarios actualizada (`UPDATE_USERS_LIST`), para reflejar quién está online/offline.
- `logEvent(type, description)` — imprime en consola, notifica a los listeners de la vista
  admin (en ≤1 s) y persiste el evento en `server_events` de forma **asíncrona** (en un
  hilo aparte para no bloquear).

### 2.3 ClientThread — un hilo por cliente

[ChatServer/src/server/ClientThread.java](../ChatServer/src/server/ClientThread.java)

Cada cliente conectado es atendido por un `ClientThread` (`Runnable`) en su propio hilo:

1. Abre `BufferedReader` (entrada) y `PrintWriter` (salida con autoflush), ambos en UTF-8.
2. Bucle `while ((receivedData = reader.readLine()) != null)` — lee cada trama JSON.
3. Parsea con `mapper.readTree(receivedData)`, lee el campo `type` y hace **dispatch**.
4. Guarda `userEmail` cuando el usuario hace login (para saber a quién pertenece el socket).

```java
public synchronized void sendMessage(String msg) {
    if (out != null) out.println(msg);   // escribe una línea JSON al cliente
}
```

**Limpieza al desconectar** (bloque `finally`): si había un usuario logueado, lo marca como
offline, borra sus conversaciones temporales y notifica a los demás:

```java
finally {
    if (this.userEmail != null) {
        UserDAO userDAO = new UserDAO();
        userDAO.changeIsConnected(this.userEmail, false);
        User droppedUser = userDAO.getUserByEmail(this.userEmail);
        if (droppedUser != null)
            cleanupTempConversations(droppedUser.getIdUser(), new ObjectMapper());
    }
    server.removeClient(clientId);
    server.broadcastUserStatus();   // todos ven el cambio de estado
}
```

### 2.4 AdminView

[ChatServer/src/view/AdminView.java](../ChatServer/src/view/AdminView.java) — ventana Swing
que muestra en tiempo real el log de eventos del servidor (`SERVER_START`, conexiones,
`GROUP_DELETED`, etc.). Se suscribe a `JavaServer` mediante un `Consumer<String>` listener.

---

## 3. Arquitectura del cliente

El cliente sigue **MVC real**: las vistas solo dibujan, los controllers tienen la lógica y
la red, y el ruteo de mensajes entrantes está centralizado.

### 3.1 Punto de entrada

[ChatClient/src/chatclient/ChatClient.java](../ChatClient/src/chatclient/ChatClient.java)

```java
public static void main(String[] args) {
    LoginView loginView = new LoginView();
    loginView.setVisible(true);
}
```

### 3.2 ClientSocket — Singleton de red

[ChatClient/src/socket/ClientSocket.java](../ChatClient/src/socket/ClientSocket.java)

- **Singleton** que centraliza toda la red del cliente.
- Carga `CHAT_HOST`/`CHAT_PORT` desde `.env`, con fallback a variables de entorno y por
  último `127.0.0.1:1235`.
- `tryConnect()` conecta en un **hilo secundario** y reintenta cada 2 s si falla (para no
  congelar la UI).
- `listen()` lee líneas del socket de forma bloqueante y entrega cada JSON crudo al
  dispatcher:

```java
private void listen() {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream(), UTF_8));
    String receivedLine;
    while ((receivedLine = reader.readLine()) != null) {
        ServerDispatcher.getInstance().dispatch(receivedLine);
    }
}

public void sendText(String s) {
    out.write((s + "\n").getBytes(UTF_8));   // \n = delimitador de trama
    out.flush();
}
```

### 3.3 ServerDispatcher — router de mensajes entrantes

[ChatClient/src/socket/ServerDispatcher.java](../ChatClient/src/socket/ServerDispatcher.java)

Desacopla el socket de las vistas. Mantiene un mapa `type → lista de handlers` y, cuando
llega un mensaje, lo entrega a todos los handlers registrados para ese `type`, **siempre en
el Event Dispatch Thread (EDT)** de Swing (porque Swing no es thread-safe):

```java
private final Map<String, List<Consumer<JsonNode>>> handlers = new ConcurrentHashMap<>();

public void dispatch(String rawJson) {
    JsonNode root = Json.mapper().readTree(rawJson);
    String type = root.get("type").asText();
    List<Consumer<JsonNode>> typeHandlers = handlers.get(type);
    SwingUtilities.invokeLater(() -> {
        for (Consumer<JsonNode> handler : typeHandlers) handler.accept(root);
    });
}
```

Permite que **varios handlers** escuchen el mismo `type` (p. ej. un `SEND_MESSAGE` puede
interesar tanto al `ChatController` abierto como al `UsersListController` para pintar el
indicador "Mensaje nuevo…").

### 3.4 Controllers

| Controller | Archivo | Responsabilidad |
|------------|---------|-----------------|
| `LoginController` | [controller/LoginController.java](../ChatClient/src/controller/LoginController.java) | Login; cuenta 3 intentos fallidos → recuperación. Guarda el usuario en `SessionManager`. |
| `UsersListController` | [controller/UsersListController.java](../ChatClient/src/controller/UsersListController.java) | Carga usuarios/amigos/grupos; envía solicitudes de amistad; indicador "Mensaje nuevo…"; logout. |
| `ChatController` | [controller/ChatController.java](../ChatClient/src/controller/ChatController.java) | Chat directo: enviar/recibir, historial, cierre por `DELETE_CHAT`. Diferencia TODOS vs AMIGOS. |
| `GroupsController` | [controller/GroupsController.java](../ChatClient/src/controller/GroupsController.java) | Mensajes de grupo, historial, miembros, abandonar/eliminar grupo. |

Cada controller registra sus handlers en el `ServerDispatcher` y los **des-registra**
(`dispose()`) al cerrar la vista, evitando fugas y handlers duplicados.

### 3.5 Vistas Swing

| Vista | Función |
|-------|---------|
| `LoginView` | Inicio de sesión + estado de conexión. |
| `SignUpView` / `RecoverPasswordView` | Registro / recuperar contraseña. |
| `UsersListView` | Pantalla principal post-login con pestañas **TODOS / AMIGOS / GRUPOS**. |
| `BaseChatView` | Clase abstracta con la UI común de chat (burbujas, input, auto-scroll). |
| `ChatView` | Chat directo 1 a 1 (hereda de `BaseChatView`). |
| `GroupChatView` | Chat de grupo, con color por autor y menú de opciones. |
| `NewGroupView` / `FriendsRequestView` | Crear grupo / solicitudes de amistad. |

`BaseChatView` dibuja las burbujas con `Graphics2D` (rectángulos redondeados) y las alinea a
la derecha si el mensaje es propio o a la izquierda si es del otro.

### 3.6 Modelos y SessionManager

- `User`, `Message`, `Chat`, `Group`, `UserGroup` en [model/](../ChatClient/src/model/).
- `TodosMessage` — espejo de `Message` usado para deserializar el JSON entrante del servidor.
- `SessionManager` (Singleton) guarda el `currentUser` logueado; `logout()` lo limpia.
- `model/dbrequest/` — DTOs salientes que fijan su `type` (p. ej. `LoginRequest` → `"LOGIN"`).
- [util/Json.java](../ChatClient/src/util/Json.java) expone **un único `ObjectMapper`**
  compartido (es thread-safe y costoso de crear).

---

## 4. Protocolo de comunicación

**Formato:** un objeto JSON por línea, terminado en `\n`. El receptor usa
`BufferedReader.readLine()`, que se bloquea hasta tener la trama completa → el JSON nunca
llega partido. El campo discriminador es **`type`**.

### 4.1 Tipos de mensaje (cliente → servidor)

| `type` | Datos | Respuesta del servidor |
|--------|-------|------------------------|
| `LOGIN` | email, password | `LOGIN_SUCCESS` / `LOGIN_ERROR` |
| `SIGNUP` | name, email, password | `SIGNUP_SUCCESS` / `SIGNUP_ERROR` |
| `RECOVER_PASSWORD` | email, password | `RECOVER_PASSWORD_SUCCESS` / error |
| `LOGOUT_REQUEST` | email | `LOGOUT_SUCCESS` (+ limpia conversaciones TEMP) |
| `FETCH_ALL_USERS` | email | `UPDATE_USERS_LIST` |
| `SEND_MESSAGE` | `chat_type` TODOS/GROUP + payload | eco a destinatarios conectados |
| `FETCH_CONVERSATION_HISTORY` | userId, otherUserId, access_mode | `CONVERSATION_HISTORY` |
| `SEND_FRIEND_REQUEST` | senderId, receiverEmail | `FRIEND_REQUEST_SENT` |
| `FETCH_FRIEND_REQUESTS` | userId | `FRIEND_REQUESTS_LIST` |
| `RESPOND_FRIEND_REQUEST` | idFriendship, accept | `FRIEND_REQUEST_RESPONDED` |
| `FETCH_FRIENDS` | userId | `FRIENDS_LIST` |
| `CREATE_GROUP` | ownerId, title, invitedEmails | `GROUP_CREATED` |
| `INVITE_TO_GROUP` | groupId, invitedEmail | `GROUP_INVITE_SENT` |
| `FETCH_GROUP_INVITATIONS` | userId | `GROUP_INVITATIONS_LIST` |
| `RESPOND_GROUP_INVITATION` | idInvitation, accept | `GROUP_INVITATION_RESPONDED` |
| `FETCH_GROUPS` | userId | `GROUPS_LIST` |
| `FETCH_GROUP_MEMBERS` | groupId | `GROUP_MEMBERS` |
| `FETCH_GROUP_HISTORY` | groupId | `GROUP_HISTORY` |
| `LEAVE_GROUP` | groupId, userId | `GROUP_LEFT_OK` (+ posible `GROUP_DELETED`) |
| `DELETE_GROUP` | groupId, userId | notificación a miembros |

### 4.2 Ejemplos

**Login (request / response):**
```json
{ "type": "LOGIN", "email": "ana@ex.com", "password": "1234" }
```
```json
{ "type": "LOGIN_SUCCESS", "message": "Bienvenido",
  "user": { "idUser": 1, "name": "Ana", "email": "ana@ex.com",
            "isConnected": true, "lastAccess": "2026-06-08 10:30:00" } }
```

**Mensaje directo (TODOS efímero):**
```json
{ "type": "SEND_MESSAGE", "chat_type": "TODOS", "access_mode": "TODOS",
  "message": {
    "userSender":   { "idUser": 1, "email": "ana@ex.com" },
    "userReceiver": { "idUser": 2, "email": "bob@ex.com" },
    "text": "Hola Bob", "sendedAt": "2026-06-08 10:35:45" } }
```

**Mensaje de grupo:**
```json
{ "type": "SEND_MESSAGE", "chat_type": "GROUP", "group_id": 5,
  "sender": { "idUser": 1, "name": "Ana" }, "text": "Hola grupo",
  "sendedAt": "2026-06-08 10:36:00" }
```

**Aviso de cierre de chat efímero (servidor → cliente):**
```json
{ "type": "DELETE_CHAT", "otherUserId": 1, "access_mode": "TODOS" }
```

> El campo **`access_mode`** (`TODOS` vs `AMIGOS`) es clave: distingue conversaciones
> efímeras de persistentes y permite que el cliente filtre mensajes según la pestaña/ventana
> abierta.

---

## 5. Base de datos y capa DAO

### 5.1 Esquema (`ChatServer/schema.sql`)

Esquema canónico, **todo en inglés**, idempotente (`IF NOT EXISTS`):

| Tabla | Contenido | Notas |
|-------|-----------|-------|
| `users` | id, name, email (UNIQUE), password, is_connected, last_access | email único para login. |
| `friendships` | sender, receiver, status `PENDING/APPROVED/DENIED`, created_at | Solicitudes y amistades en una sola tabla. |
| `chat_groups` | id, title, id_owner, created_at | `groups` es palabra reservada → `chat_groups`. |
| `group_invitations` | id_group, id_invited, status | Invitaciones a grupo. |
| `conversations` | id, id_group (NULL si directa), type `TEMP/FRIEND/GROUP`, last_seen | Una conversación por contexto. |
| `conversation_members` | (id_user, id_conversation) PK | Tabla pivote usuario↔conversación. |
| `messages` | id_conversation, id_sender, content, sent_date | Mensajes persistidos. |
| `server_events` | type, description, created_at | Log para el panel de administración. |

Relaciones con `ON DELETE CASCADE`: borrar una conversación elimina sus miembros y mensajes;
borrar un grupo elimina su conversación, invitaciones y mensajes.

```
users 1───* friendships *───1 users
users 1───* chat_groups 1───* group_invitations *───1 users
chat_groups 1───1 conversations 1───* conversation_members *───1 users
conversations 1───* messages *───1 users (sender)
```

### 5.2 DAOs

Todos heredan de [DatabaseConnection](../ChatServer/src/database/DatabaseConnection.java),
que abre la conexión JDBC leyendo `DB_URL`/`DB_USER`/`DB_PASSWORD` del `.env`. Usan
`PreparedStatement` (protección contra inyección SQL).

| DAO | Métodos representativos |
|-----|-------------------------|
| `UserDAO` | `insertUser`, `getUserByEmail`, `getAllUsersNotEmail`, `changePassword`, `changeIsConnected` |
| `MessageDAO` | `insertMessage`, `getMessagesByConversation` |
| `ConversationDAO` | `createDirectConversation(type)`, `getConversationIdByUsersAndType`, `getDirectConversationsByUserAndType`, `deleteConversation`, `updateLastSeen` |
| `ConversationMemberDAO` | `addMember`, `getMembersByConversation`, `removeMember` |
| `FriendshipDAO` | `sendFriendRequest`, `areFriends`, `acceptRequest`, `denyRequest`, `getPendingRequests` |
| `GroupDAO` | `createGroup`, `getGroupsByUser`, `countApprovedMembers`, `isOwner`, `deleteGroup` |
| `GroupInvitationDAO` | `invite`, `getPendingInvitationsByUser`, `accept`, `deny`, `countPending` |
| `ServerEventDAO` | `insertEvent`, `getRecentEvents` |

---

## 6. Funcionalidades clave (flujos paso a paso)

### 6.1 Login (3 intentos → recuperación)

1. `LoginView` → `LoginController.login(email, pass)` serializa un `LoginRequest` y lo
   envía con `ClientSocket.sendText(...)`.
2. El `ClientThread` busca el usuario (`UserDAO.getUserByEmail`), compara contraseña y marca
   `is_connected = true`; guarda `userEmail` en el hilo.
3. Responde `LOGIN_SUCCESS` (con el `user`) o `LOGIN_ERROR`.
4. El handler `LOGIN_SUCCESS` del `LoginController` guarda el usuario en `SessionManager` y
   abre `UsersListView`. Tres `LOGIN_ERROR` seguidos → se abre `RecoverPasswordView`.

### 6.2 Lista de usuarios y estado online/offline

- Al abrir `UsersListView` se piden `fetchAllUsers()`, `fetchFriends()`, `fetchGroups()`.
- Cada vez que alguien se conecta/desconecta, el servidor hace `broadcastUserStatus()` →
  todos reciben `UPDATE_USERS_LIST` y repintan el círculo azul (online) / gris (offline).

### 6.3 Chat TODOS (efímero) vs AMIGOS (persistente) — el concepto central

Existen **dos tipos de conversación directa simultáneos** para el mismo par de usuarios,
distinguidos por `access_mode`:

| | TODOS (`TEMP`) | AMIGOS (`FRIEND`) |
|---|---|---|
| Pestaña | "TODOS" | "AMIGOS" |
| Requiere amistad | No | Sí |
| Persistencia | Se borra al desconectarse cualquiera de los dos | Permanece para siempre |
| Historial | Solo en memoria | Recuperable (`FETCH_CONVERSATION_HISTORY`) |
| Cierre automático | Sí (`DELETE_CHAT`) | No |

**Decisión de tipo al enviar** (en `ClientThread`):

```java
boolean persistent = friends && "AMIGOS".equals(accessMode);
String desiredType = persistent ? "FRIEND" : "TEMP";
int idConv = convDAO.getConversationIdByUsersAndType(idSender, idReceiver, desiredType);
if (idConv == -1) {
    idConv = convDAO.createDirectConversation(desiredType);
    memberDAO.addMember(idSender, idConv);
    memberDAO.addMember(idReceiver, idConv);
}
```

**Entrega en vivo:** el servidor persiste el mensaje y luego busca al destinatario con
`server.findClientByUserEmail(...)`; si está conectado le reenvía el JSON.

**Limpieza al logout/desconexión** (`cleanupTempConversations`): por cada conversación
`TEMP` del usuario, avisa al otro miembro conectado con un `DELETE_CHAT` y borra la
conversación de la BD. En el cliente, `ChatController` recibe `DELETE_CHAT`, comprueba que
corresponde a esa conversación y `access_mode`, y cierra la ventana volviendo a la lista.

### 6.4 Indicador "Mensaje nuevo…"

Si llega un `SEND_MESSAGE` mientras el usuario está en `UsersListView` (sin el chat abierto),
el `UsersListController` lo captura, agrega el `senderId` a `unreadUserIds` y repinta la
pestaña; ese contacto muestra "● Mensaje nuevo…" en azul hasta que se abre el chat.

### 6.5 Amistad

`SEND_FRIEND_REQUEST` crea una fila `PENDING` en `friendships`. El receptor la ve en
`FriendsRequestView` y responde con `RESPOND_FRIEND_REQUEST` (`APPROVED`/`DENIED`). Solo los
amigos (`APPROVED`) pueden tener chat persistente desde la pestaña AMIGOS.

### 6.6 Grupos y regla de permanencia (≥3 miembros)

- `CREATE_GROUP` crea el grupo, su conversación `GROUP`, agrega al owner e invita por email.
- Aceptar una invitación (`RESPOND_GROUP_INVITATION`) agrega al usuario a
  `conversation_members`.
- **Regla de permanencia:** un grupo que no pueda alcanzar 3 miembros (aprobados +
  pendientes) se **elimina automáticamente**:

```java
int approved = groupDAO.countApprovedMembers(idGroup); // incluye al creador
int pending  = invDAO.countPending(idGroup);
if (approved + pending < 3) {
    deleteGroupAndNotify(idGroup, mapper);
    server.logEvent("GROUP_DELETED", "Grupo " + idGroup + " eliminado (<3 miembros).");
}
```

Se evalúa al rechazar invitación, al abandonar el grupo, y si el owner se va el grupo se
elimina por completo. Los mensajes de grupo se persisten y se reenvían a todos los miembros
conectados **excepto** al remitente.

### 6.7 Logout

`LOGOUT_REQUEST` → el servidor marca offline, limpia TEMP y responde `LOGOUT_SUCCESS`. El
cliente limpia `SessionManager` y vuelve a `LoginView`.

---

## 7. Concurrencia y threading

### Servidor — "un hilo por cliente"

- Un hilo corre el bucle `accept()`; cada conexión obtiene su propio `ClientThread`.
- Estructuras compartidas thread-safe: `ConcurrentHashMap` (clientes activos),
  `CopyOnWriteArrayList` (listeners de eventos), `AtomicInteger` (ids).
- `sendMessage()` es `synchronized` para que dos hilos no escriban entremezclado al mismo
  socket.

### Cliente — hilo de red + EDT

- `ClientSocket` escucha en un hilo secundario para no congelar la UI.
- Como Swing **no es thread-safe**, todo lo que toca la UI pasa por
  `SwingUtilities.invokeLater(...)` dentro del `ServerDispatcher`, garantizando que los
  handlers corren en el EDT.

---

## 8. Configuración y despliegue

### Archivos `.env` (no versionados)

**ChatClient/.env**
```
CHAT_HOST=20.163.58.45    # IP de la VM Azure (o 127.0.0.1 en local)
CHAT_PORT=1235
```

**ChatServer/.env**
```
DB_URL=jdbc:mysql://...aivencloud.com:13866/defaultdb
DB_USER=avnadmin
DB_PASSWORD=********
```

### Compilación y ejecución

- Proyectos **NetBeans + Ant**: `ant jar` (o desde NetBeans). Dependen de los `.jar` de
  `lib/` (Jackson + MySQL connector).
- Servidor en producción: `deploy.sh` hace `git pull` + `ant jar` + `systemctl restart
  chatserver`.
- Aplicar el esquema: `mysql -h <host> -P <port> -u <user> -p <db> < ChatServer/schema.sql`.

---

## 9. Preguntas y respuestas tipo examen

**¿Por qué sockets TCP y no HTTP/REST?**
Porque el chat necesita comunicación **bidireccional y en tiempo real**: el servidor debe
poder *empujar* mensajes al cliente sin que este pregunte (push). HTTP es petición-respuesta;
TCP mantiene una conexión persistente que permite que el servidor envíe `SEND_MESSAGE`,
`DELETE_CHAT` o `UPDATE_USERS_LIST` en cualquier momento.

**¿Cómo se delimitan los mensajes en el stream TCP?**
Con un `\n` al final de cada JSON. El receptor usa `readLine()`, que se bloquea hasta tener
la línea completa, evitando tramas partidas o pegadas. Es un protocolo "orientado a líneas".

**¿Por qué Jackson / un solo `ObjectMapper`?**
Jackson (de)serializa objetos Java ↔ JSON. El `ObjectMapper` es costoso de crear pero
thread-safe, así que se comparte una sola instancia (`util/Json.java`).

**¿Cómo se evita corromper la UI desde el hilo de red?**
Swing no es thread-safe. El `ServerDispatcher` envuelve la ejecución de los handlers en
`SwingUtilities.invokeLater(...)`, que los corre en el Event Dispatch Thread. El hilo de red
solo lee del socket y delega.

**¿Qué modelo de concurrencia usa el servidor?**
"Un hilo por cliente": un hilo acepta conexiones y cada cliente tiene su propio
`ClientThread`. Los clientes activos viven en un `ConcurrentHashMap`.

**¿Cuál es la diferencia entre TEMP/TODOS y FRIEND/AMIGOS?**
TODOS (`TEMP`) es un chat efímero con cualquier usuario que se borra cuando alguno se
desconecta; AMIGOS (`FRIEND`) requiere amistad confirmada y persiste con historial. Se
distinguen con el campo `access_mode`. Pueden coexistir dos conversaciones (una de cada tipo)
entre el mismo par de usuarios.

**¿Cómo llega un mensaje de un usuario a otro?**
El servidor recibe el `SEND_MESSAGE`, lo persiste, busca al destinatario con
`findClientByUserEmail(...)` y, si está conectado, le reenvía el JSON por su socket. Si no
está conectado, el mensaje queda guardado (en conversaciones persistentes) para cuando pida
el historial.

**¿Qué pasa si un usuario se desconecta abruptamente (cierra la app, se cae la red)?**
`readLine()` devuelve `null`/lanza excepción; el `finally` del `ClientThread` marca al
usuario offline, ejecuta `cleanupTempConversations` (borra chats TEMP y avisa con
`DELETE_CHAT`), lo quita del mapa y hace `broadcastUserStatus()`.

**¿Cómo se actualiza el estado online/offline en tiempo real?**
Cada conexión/desconexión dispara `broadcastUserStatus()`, que envía `UPDATE_USERS_LIST` a
todos los clientes activos; estos repintan el indicador de estado.

**¿Cómo funciona el indicador "Mensaje nuevo…"?**
Si llega un `SEND_MESSAGE` sin el chat abierto, el `UsersListController` guarda el `senderId`
en `unreadUserIds` y repinta la lista marcando a ese contacto.

**¿Por qué un grupo se borra solo? (regla de permanencia)**
Por requisito: un grupo debe poder tener al menos 3 miembros. Si entre aprobados y pendientes
no suman 3 (p. ej. tras un rechazo o un abandono), se elimina automáticamente y se notifica.

**¿Cómo se protege contra inyección SQL?**
Todos los DAOs usan `PreparedStatement` con parámetros, nunca concatenan valores en el SQL.

**¿Cómo se evitan fugas de handlers en el cliente?**
Cada controller registra sus handlers en el `ServerDispatcher` al abrir la vista y los
des-registra en `dispose()` al cerrarla.

**¿Cómo se configura a qué servidor se conecta el cliente?**
Por `.env` (`CHAT_HOST`/`CHAT_PORT`), con fallback a variables de entorno y a
`127.0.0.1:1235`. Así el mismo binario sirve para local y producción sin recompilar.

**Limitaciones / mejoras conocidas (honestidad técnica):**
las contraseñas se guardan en texto plano (debería usarse BCrypt/Argon2); cada DAO abre su
propia conexión JDBC (un pool sería más eficiente); algunas respuestas del servidor se
construyen como strings JSON a mano (propenso a typos). Ver
[reporte-tecnico.md §9](./reporte-tecnico.md).
