# Diagramas de Secuencia — App de Mensajería (LightChat)

Arquitectura cliente-servidor basada en sockets TCP, con intercambio de mensajes
en formato JSON (serializados/deserializados con Jackson). Cada respuesta termina
con un salto de línea (`\n`) que delimita el mensaje.

## 1. Arranque y conexión del cliente

```mermaid
sequenceDiagram
    actor Usuario
    participant LV as LoginView (Swing)
    participant CS as ClientSocket (Singleton)
    participant Net as Hilo de red
    participant JS as JavaServer
    participant CT as ClientThread

    Usuario->>LV: Ejecuta la app
    LV->>CS: getInstance()
    LV->>CS: setStatusListener(...) / setMessageListener(...)
    LV->>CS: tryConnect()
    CS->>Net: new Thread(listen)
    Net->>JS: new Socket(host, port)
    JS->>JS: ss.accept()
    JS->>CT: new ClientThread(socket, id, this)
    JS->>CT: new Thread(client).start()
    CT-->>JS: writeConsole("CONNECTED")
    Net-->>CS: updateStatus("Conectado")
    CS-->>LV: onStatusUpdate.accept("Conectado")
    Note over Net: listen() queda bloqueado<br/>en reader.readLine()
```

## 2. Inicio de sesión (LOGIN)

```mermaid
sequenceDiagram
    actor Usuario
    participant LV as LoginView
    participant CS as ClientSocket
    participant CT as ClientThread
    participant DAO as UserDAO
    participant DB as Base de datos

    Usuario->>LV: Click "Log in" (email, password)
    LV->>LV: Valida campos no vacíos
    LV->>LV: new LoginRequest(email, pass)
    LV->>LV: ObjectMapper.writeValueAsString(req)
    LV->>CS: sendText(jsonString)
    CS->>CT: {"type":"LOGIN", email, password}\n

    CT->>CT: mapper.readTree() -> type = LOGIN
    CT->>DAO: getUserByEmail(email)
    DAO->>DB: SELECT * FROM users WHERE email = ?
    DB-->>DAO: ResultSet
    DAO-->>CT: User (o null)

    alt Credenciales correctas
        CT-->>CS: {"type":"LOGIN_SUCCESS"}\n
        CS->>CS: SwingUtilities.invokeLater
        CS-->>LV: onMessageReceived.accept(json)
        LV->>LV: abre UsersListView, dispose()
    else Credenciales incorrectas
        CT-->>CS: {"type":"LOGIN_ERROR", message}\n
        CS-->>LV: onMessageReceived.accept(json)
        LV-->>Usuario: JOptionPane error
        Note over LV: Tras 3 errores abre RecoverPasswordView
    end
```

## 3. Registro de usuario (SIGNUP)

```mermaid
sequenceDiagram
    actor Usuario
    participant SV as SignUpView
    participant CS as ClientSocket
    participant CT as ClientThread
    participant DAO as UserDAO
    participant DB as Base de datos

    Usuario->>SV: Click "Registrar" (name, email, pass, state)
    SV->>SV: new SignUpRequest(...)
    SV->>SV: ObjectMapper.writeValueAsString(req)
    SV->>CS: sendText(jsonString)
    CS->>CT: {"type":"SIGNUP", name, email, password, state}\n

    CT->>CT: type = SIGNUP
    CT->>DAO: insertUser(new User(...))
    DAO->>DB: INSERT INTO users (...)
    DB-->>DAO: OK
    CT-->>CS: {"type":"SIGNUP_SUCCESS"}\n
    CS-->>SV: onMessageReceived.accept(json)
    SV-->>Usuario: Confirma registro / vuelve a Login
```

## 4. Recuperar contraseña (RECOVER_PASSWORD)

```mermaid
sequenceDiagram
    actor Usuario
    participant RV as RecoverPasswordView
    participant CS as ClientSocket
    participant CT as ClientThread
    participant DAO as UserDAO
    participant DB as Base de datos

    Usuario->>RV: Ingresa nueva contraseña
    RV->>CS: sendText({"type":"RECOVER_PASSWORD", email, password})
    CS->>CT: JSON\n

    CT->>CT: type = RECOVER_PASSWORD
    CT->>DAO: changePassword(email, newPassword)
    DAO->>DB: SELECT (¿existe usuario?)
    DB-->>DAO: User / null
    alt Usuario existe
        DAO->>DB: UPDATE users SET password = ? WHERE email = ?
        DB-->>DAO: OK
        DAO-->>CT: true
        CT-->>CS: {"type":"RECOVER_PASSWORD_SUCCESS"}\n
        CS-->>RV: vuelve a Login
    else No existe
        DAO-->>CT: false
        CT-->>CS: {"type":"RECOVER_PASSWROD_ERROR", message}\n
        CS-->>RV: JOptionPane error
    end
```

## 5. Desconexión del cliente

```mermaid
sequenceDiagram
    participant CS as ClientSocket
    participant CT as ClientThread
    participant JS as JavaServer

    Note over CS: Se cierra el socket / pérdida de conexión
    CS->>CS: readLine() devuelve null -> IOException
    CS->>CS: closeSocket(), updateStatus("Desconectado")
    CT->>CT: readLine() devuelve null (finally)
    CT->>JS: removeClient(clientId)
    JS-->>JS: activeClients.remove(id)
```
