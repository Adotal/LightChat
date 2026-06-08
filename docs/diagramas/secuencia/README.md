# Diagramas de Secuencia — App de Mensajería PA-II

Diagramas UML de secuencia (PlantUML) que documentan los flujos de comunicación
**extremo a extremo** entre el cliente (`ChatClient`, Swing) y el servidor
(`ChatServer`, sockets + MySQL). La comunicación es **JSON sobre TCP** con líneas
terminadas en `\n`.

## Arquitectura de referencia

- **Cliente:** `View` → `Controller` → `ClientSocket` (envío) y
  `ClientSocket (listen)` → `ServerDispatcher` (enruta por campo `type`) →
  `Controller` → `View` (recepción, en el EDT de Swing).
- **Servidor:** `JavaServer` acepta conexiones y crea un `ClientThread` por
  cliente; `ClientThread` parsea el JSON, lo rutea y usa los DAOs para
  persistir/consultar en MySQL. `JavaServer.broadcastUserStatus()` y
  `findClientByUserEmail()` permiten reenviar mensajes entre clientes.
- **Tipos de conversación:** `TEMP` (efímera, chat "Todos", se borra al logout),
  `FRIEND` (persistente entre amigos), `GROUP` (grupos).

## Índice de diagramas

| # | Archivo | Flujo | Fuentes principales |
|---|---------|-------|---------------------|
| 01 | [01-registro.puml](01-registro.puml) | Registro de usuario (SIGNUP) | `SignUpController`, `ClientThread`, `UserDAO` |
| 02 | [02-login.puml](02-login.puml) | Inicio de sesión + broadcast estado | `LoginController`, `ClientThread`, `UserDAO`, `JavaServer` |
| 03 | [03-conexion-socket.puml](03-conexion-socket.puml) | Conexión/escucha del socket (infraestructura) | `ClientSocket`, `ServerDispatcher`, `JavaServer` |
| 04 | [04-enviar-mensaje-todos.puml](04-enviar-mensaje-todos.puml) | Enviar mensaje chat TODOS (TEMP) | `ChatController`, `ClientThread`, `MessageDAO` |
| 05 | [05-enviar-mensaje-amigos.puml](05-enviar-mensaje-amigos.puml) | Enviar mensaje chat AMIGOS (FRIEND) | `ChatController`, `ClientThread`, `FriendshipDAO`, `MessageDAO` |
| 06 | [06-recibir-mensaje.puml](06-recibir-mensaje.puml) | Recibir mensaje + indicador "Mensaje nuevo..." | `ServerDispatcher`, `ChatController`, `UsersListController` |
| 07 | [07-historial-conversacion.puml](07-historial-conversacion.puml) | Historial de conversación directa | `ChatController`, `ClientThread`, `ConversationDAO`, `MessageDAO` |
| 08 | [08-logout.puml](08-logout.puml) | Cierre de sesión + limpieza TEMP | `UsersListController`, `ClientThread`, `UserDAO` |
| 09 | [09-lista-usuarios-broadcast.puml](09-lista-usuarios-broadcast.puml) | Lista de usuarios y broadcast de estado | `UsersListController`, `ClientThread`, `JavaServer`, `UserDAO` |
| 10 | [10-enviar-solicitud-amistad.puml](10-enviar-solicitud-amistad.puml) | Enviar solicitud de amistad | `UsersListController`, `ClientThread`, `FriendshipDAO` |
| 11 | [11-responder-solicitud-amistad.puml](11-responder-solicitud-amistad.puml) | Aceptar/rechazar solicitud | `FriendsRequestController`, `ClientThread`, `FriendshipDAO` |
| 12 | [12-listar-solicitudes-amigos.puml](12-listar-solicitudes-amigos.puml) | Listar solicitudes y amigos | `FriendsRequestController`, `UsersListController`, `FriendshipDAO` |
| 13 | [13-crear-grupo.puml](13-crear-grupo.puml) | Crear grupo + invitaciones | `NewGroupController`, `ClientThread`, `GroupDAO`, `GroupInvitationDAO` |
| 14 | [14-responder-invitacion-grupo.puml](14-responder-invitacion-grupo.puml) | Responder invitación (regla ≥3 miembros) | `FriendsRequestController`, `ClientThread`, `GroupInvitationDAO` |
| 15 | [15-mensaje-grupo.puml](15-mensaje-grupo.puml) | Enviar mensaje de grupo | `GroupsController`, `ClientThread`, `MessageDAO` |
| 16 | [16-historial-grupo.puml](16-historial-grupo.puml) | Historial y miembros de grupo | `GroupsController`, `ClientThread`, `MessageDAO` |
| 17 | [17-abandonar-eliminar-grupo.puml](17-abandonar-eliminar-grupo.puml) | Abandonar / eliminar grupo | `GroupsController`, `ClientThread`, `GroupDAO` |

## Cómo renderizar

- **VSCode:** instala la extensión *PlantUML* (jebbs). Abre un `.puml` y usa
  `Alt+D` para previsualizar.
- **En línea:** copia el contenido en https://www.plantuml.com/plantuml.
- **CLI (requiere Java + Graphviz):**
  ```bash
  plantuml docs/diagramas/secuencia/*.puml        # genera PNG
  plantuml -tsvg docs/diagramas/secuencia/*.puml  # genera SVG
  plantuml -checkonly docs/diagramas/secuencia/*.puml  # solo validar sintaxis
  ```
