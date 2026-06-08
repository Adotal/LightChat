# Requerimientos

> **Criterio.** Los **requerimientos funcionales (RQF)** describen lo que el usuario hace o ve en el sistema (acciones, confirmaciones y mensajes que percibe). Los **requerimientos no funcionales (RQNF)** describen validaciones internas y atributos de calidad (rendimiento, usabilidad, seguridad, fiabilidad, persistencia).

## Registro de usuario

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF1:** El usuario se registra en el sistema mediante un nombre de usuario, correo electrónico y una contraseña.<br>**RQF2:** El usuario visualiza un mensaje que indica el motivo del rechazo cuando su registro no se completa.<br>**RQF3:** El usuario visualiza un mensaje de error si ocurre un fallo durante el registro, sin que la aplicación se cierre. | **RQNF1:** El sistema valida que el campo de correo electrónico no esté vacío al registrarse.<br>**RQNF2:** El sistema valida que el campo de nombre de usuario no esté vacío al registrarse.<br>**RQNF3:** El sistema valida que el campo de contraseña no esté vacío al registrarse.<br>**RQNF4:** El sistema valida que la contraseña tenga mínimo 4 caracteres.<br>**RQNF5:** El sistema verifica que el correo electrónico no se encuentre registrado previamente.<br>**RQNF6 (Seguridad):** El sistema almacena la contraseña en texto plano. |

## Inicio de sesión

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF4:** El usuario registrado ingresa su correo electrónico y contraseña para iniciar sesión.<br>**RQF5:** El usuario visualiza un mensaje de error cuando las credenciales no coinciden.<br>**RQF6:** El usuario visualiza un mensaje de error si ocurre un fallo durante el inicio de sesión, sin que la aplicación se cierre. | **RQNF7:** El sistema valida que el campo de correo electrónico no esté vacío.<br>**RQNF8:** El sistema verifica que el correo electrónico exista en la base de datos.<br>**RQNF9:** El sistema valida que el campo de contraseña no esté vacío.<br>**RQNF10:** El sistema valida contra la base de datos que las credenciales ingresadas sean correctas.<br>**RQNF11 (Rendimiento):** El sistema resuelve el intento de autenticación en un tiempo no mayor a 2 segundos. |

## Recuperación de contraseña

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF7:** El usuario accede a la vista de recuperación de contraseña después de tres intentos de inicio de sesión fallidos consecutivos.<br>**RQF8:** El usuario es redirigido a la vista de inicio de sesión después de un cambio de contraseña exitoso.<br>**RQF9:** El usuario visualiza una confirmación de que su contraseña se cambió correctamente.<br>**RQF10:** El usuario visualiza un mensaje de error si ocurre un fallo durante el cambio de contraseña, sin que la aplicación se cierre. | **RQNF12:** El sistema toma el correo ingresado en la vista de inicio de sesión como la cuenta a modificar.<br>**RQNF13:** El sistema valida que el campo de la nueva contraseña no esté vacío.<br>**RQNF14:** El sistema actualiza la contraseña en la base de datos una vez finalizado el proceso. |

## Registro de eventos de autenticación

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF11:** El sistema registra los intentos fallidos de autenticación y los imprime en el log del servidor. | **RQNF15 (Rendimiento):** El sistema imprime en el log del servidor cada evento de error de autenticación en un tiempo no mayor a 1 segundo desde que se detectan las credenciales incorrectas. |

## Pestaña "Todos"

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF12:** El usuario visualiza en la pestaña "Todos" la lista de todos los usuarios registrados en el sistema. | **RQNF16 (Usabilidad):** El sistema muestra un color particular dependiendo del estado actual de cada usuario.<br>**RQNF17:** El sistema establece el estado de conexión de un usuario a *conectado* cuando inicia sesión.<br>**RQNF18:** El sistema establece el estado de conexión de un usuario a *desconectado* cuando cierra sesión.<br>**RQNF19 (Fiabilidad):** El estado de conexión mostrado refleja el estado real con un desfase no mayor a 2 segundos. |

## Conversaciones en "Chat Todos"

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF13:** El usuario puede seleccionar a otro usuario para iniciar una conversación.<br>**RQF14:** El usuario puede enviar mensajes a usuarios dentro de la vista "Chat Todos".<br>**RQF15:** El usuario puede recibir mensajes desde "Chat Todos".<br>**RQF16:** El usuario visualiza un mensaje de error si ocurre un fallo al enviar un mensaje, sin que la aplicación se cierre. | **RQNF20:** El sistema permite seleccionar únicamente usuarios conectados.<br>**RQNF21:** El sistema abre una ventana de conversación al seleccionar un usuario.<br>**RQNF22:** El sistema valida que el mensaje no esté vacío antes de enviarlo.<br>**RQNF23:** El sistema registra temporalmente el mensaje durante la sesión activa.<br>**RQNF24:** El sistema muestra automáticamente los mensajes recibidos cuando el usuario está dentro de la conversación.<br>**RQNF25 (Rendimiento):** El sistema entrega un mensaje a un destinatario conectado en un tiempo no mayor a 1 segundo. |

## Cierre de sesión

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF17:** El usuario puede cerrar sesión. | **RQNF26:** El sistema elimina el historial de conversación del usuario con los usuarios no amigos.<br>**RQNF27 (Privacidad):** Las conversaciones con usuarios no amigos son efímeras: no se recuperan en sesiones posteriores.<br>**RQNF28 (Fiabilidad):** Tras el cierre de sesión, el estado del usuario pasa a *desconectado* y se propaga a los demás clientes en un tiempo no mayor a 2 segundos. |

## Solicitudes de amistad

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF18:** El usuario envía solicitudes de amistad desde la vista de todos.<br>**RQF19:** El usuario visualiza las invitaciones de amistad enviadas.<br>**RQF20:** El usuario visualiza las invitaciones de amistad recibidas.<br>**RQF21:** El usuario puede aceptar o rechazar invitaciones de amistad recibidas.<br>**RQF22:** El usuario visualiza un mensaje de error si ocurre un fallo al gestionar solicitudes, sin que la aplicación se cierre. | **RQNF29:** El sistema registra la solicitud de amistad enviada.<br>**RQNF30 (Disponibilidad):** El sistema notifica al usuario receptor una nueva solicitud en un tiempo no mayor a 2 segundos.<br>**RQNF31:** El sistema notifica que un usuario ya es amigo cuando se intenta enviarle una solicitud.<br>**RQNF32:** El sistema impide enviar solicitudes a usuarios ya agregados como amigos.<br>**RQNF33:** El sistema muestra el estado actual de cada invitación enviada: ACEPTADO, RECHAZADO, PENDIENTE.<br>**RQNF34:** El sistema actualiza el estado de la invitación cuando el destinatario la acepta o rechaza.<br>**RQNF35:** El sistema actualiza la lista de invitaciones recibidas cuando llega una nueva solicitud.<br>**RQNF36:** El sistema muestra el usuario remitente de cada invitación.<br>**RQNF37:** El sistema registra la respuesta seleccionada por el usuario en la base de datos.<br>**RQNF38:** El sistema agrega automáticamente a ambos usuarios a sus respectivas listas de amigos cuando la invitación es aceptada.<br>**RQNF39 (Usabilidad):** El sistema representa los estados (ACEPTADO/RECHAZADO/PENDIENTE) con colores consistentes en todas las vistas. |

## Pestaña "Amigos"

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF23:** El usuario visualiza la lista de usuarios que aceptaron su solicitud de amistad en la pestaña "Amigos". | **RQNF40:** El sistema muestra únicamente los usuarios con una amistad confirmada.<br>**RQNF41:** El sistema actualiza automáticamente la lista cuando una solicitud de amistad es aceptada.<br>**RQNF42:** El sistema muestra el estado de conexión de cada amigo.<br>**RQNF43 (Usabilidad):** El sistema indica el estado de conexión de cada amigo mediante un indicador visual. |

## Mensajería con amigos

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF24:** El usuario puede enviar mensajes a sus amigos.<br>**RQF25:** El usuario puede recibir mensajes de sus amigos.<br>**RQF26:** El usuario puede visualizar las conversaciones previas de "Chat Amigos" después de iniciar sesión nuevamente.<br>**RQF27:** El usuario visualiza un mensaje de error si ocurre un fallo al enviar un mensaje, sin que la aplicación se cierre. | **RQNF44:** El sistema valida que el mensaje no esté vacío antes de enviarlo.<br>**RQNF45:** El sistema entrega el mensaje al destinatario seleccionado.<br>**RQNF46:** El sistema muestra automáticamente los mensajes recibidos en la conversación correspondiente.<br>**RQNF47:** El sistema almacena los mensajes de "Chat Amigos".<br>**RQNF48:** El sistema recupera las conversaciones almacenadas cuando el usuario vuelve a iniciar sesión.<br>**RQNF49 (Usabilidad):** El sistema muestra el historial de mensajes en orden cronológico.<br>**RQNF50 (Rendimiento):** El sistema entrega un mensaje a un amigo conectado en un tiempo no mayor a 1 segundo.<br>**RQNF51 (Persistencia):** Los mensajes con amigos persisten entre sesiones. |

## Creación y gestión de grupos

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF28:** El usuario puede crear un grupo.<br>**RQF29:** El usuario puede invitar usuarios a un grupo.<br>**RQF30:** El usuario puede visualizar las invitaciones de grupo recibidas.<br>**RQF31:** El usuario puede visualizar las personas invitadas a un grupo y el estado de sus invitaciones.<br>**RQF32:** El usuario puede aceptar o rechazar invitaciones de grupo recibidas.<br>**RQF33:** El usuario puede visualizar los grupos de los que forma parte.<br>**RQF34:** El usuario puede abandonar un grupo en cualquier momento.<br>**RQF35:** El creador de un grupo puede eliminar el grupo.<br>**RQF36:** El creador de un grupo puede abandonar el grupo.<br>**RQF37:** El usuario visualiza un mensaje de error si ocurre un fallo en una operación de grupo, sin que la aplicación se cierre. | **RQNF52:** El sistema genera un identificador único para el grupo creado.<br>**RQNF53:** El sistema asigna automáticamente el rol de administrador al usuario que creó el grupo.<br>**RQNF54:** El sistema registra la información del grupo.<br>**RQNF55:** El sistema permite enviar invitaciones únicamente a usuarios registrados.<br>**RQNF56:** El sistema registra las invitaciones enviadas para unirse al grupo.<br>**RQNF57 (Disponibilidad):** El sistema notifica a los usuarios una invitación de grupo en un tiempo no mayor a 2 segundos.<br>**RQNF58:** El sistema muestra únicamente las invitaciones asociadas al usuario autenticado.<br>**RQNF59:** El sistema actualiza la lista cuando se recibe una nueva invitación.<br>**RQNF60:** El sistema muestra el nombre del grupo asociado a cada invitación.<br>**RQNF61:** El sistema muestra el estado de cada invitación: ACEPTADO, RECHAZADO, PENDIENTE.<br>**RQNF62:** El sistema actualiza automáticamente el estado de las invitaciones cuando los usuarios responden.<br>**RQNF63:** El sistema muestra únicamente los usuarios invitados al grupo seleccionado.<br>**RQNF64 (Usabilidad):** El sistema representa los estados de invitación con colores consistentes en todas las vistas.<br>**RQNF65:** El sistema registra la respuesta seleccionada por el usuario.<br>**RQNF66:** El sistema agrega automáticamente al usuario como miembro del grupo cuando la invitación es aceptada.<br>**RQNF67:** El sistema actualiza la lista de participantes visibles para los integrantes del grupo.<br>**RQNF68:** El sistema actualiza la lista de grupos cuando el usuario es agregado o eliminado de un grupo.<br>**RQNF69:** El sistema elimina al usuario de la lista de miembros cuando abandona el grupo.<br>**RQNF70:** El sistema actualiza la información de participantes del grupo después de la salida de un usuario.<br>**RQNF71 (Permanencia):** El grupo permanece activo únicamente si cuenta con al menos tres miembros con invitación ACEPTADA, incluido el creador.<br>**RQNF72 (Permanencia):** El sistema elimina el grupo cuando no se alcanza el mínimo de tres miembros aceptados, aunque existan invitaciones pendientes, y elimina también las invitaciones asociadas.<br>**RQNF73 (Autorización):** Únicamente el creador del grupo puede eliminarlo.<br>**RQNF74:** El sistema elimina el grupo seleccionado de forma permanente.<br>**RQNF75:** El sistema elimina las invitaciones asociadas al grupo eliminado.<br>**RQNF76:** El sistema notifica a los integrantes que el grupo ha sido eliminado.<br>**RQNF77:** El sistema elimina automáticamente el grupo cuando el creador lo abandona.<br>**RQNF78:** El sistema elimina los registros asociados al grupo una vez que éste deja de existir.<br>**RQNF79 (Consistencia):** El sistema evalúa las reglas de permanencia de forma atómica tras cada cambio de membresía. |

## Mensajería en grupos

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF38:** El usuario puede visualizar el historial de mensajes de un grupo al ingresar.<br>**RQF39:** El usuario puede enviar mensajes dentro de un grupo.<br>**RQF40:** El usuario puede recibir mensajes dentro de un grupo.<br>**RQF41:** El usuario visualiza un mensaje de error si ocurre un fallo al enviar un mensaje, sin que la aplicación se cierre. | **RQNF80:** El sistema recupera los mensajes almacenados del grupo seleccionado.<br>**RQNF81:** El sistema muestra el historial completo disponible para los integrantes del grupo.<br>**RQNF82 (Usabilidad):** El sistema muestra los mensajes en orden cronológico.<br>**RQNF83:** El sistema valida que el mensaje no esté vacío antes de enviarlo.<br>**RQNF84:** El sistema distribuye el mensaje a todos los integrantes activos del grupo.<br>**RQNF85:** El sistema almacena el mensaje en el historial del grupo.<br>**RQNF86:** El sistema muestra automáticamente los mensajes recibidos.<br>**RQNF87:** El sistema identifica al remitente de cada mensaje.<br>**RQNF88 (Rendimiento):** El sistema actualiza la conversación grupal con los nuevos mensajes en un tiempo no mayor a 2 segundos.<br>**RQNF89 (Persistencia):** El historial de mensajes del grupo persiste entre sesiones. |

## Administración del servidor

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF42:** El administrador del sistema puede visualizar las acciones realizadas por el servidor. | **RQNF90:** El sistema registra los eventos generados por los usuarios.<br>**RQNF91:** El sistema muestra los eventos registrados.<br>**RQNF92:** El sistema actualiza la información mostrada conforme ocurren nuevas acciones.<br>**RQNF93 (Rendimiento):** El sistema refleja los nuevos eventos en la vista del administrador en un tiempo no mayor a 1 segundo.<br>**RQNF94 (Usabilidad):** El sistema muestra cada evento de forma legible e incluye su marca temporal. |
