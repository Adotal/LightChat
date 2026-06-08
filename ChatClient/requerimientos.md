# Requerimientos

> **Criterio.** Los **requerimientos funcionales (RQF)** describen las acciones que el usuario/sistema ejecuta. Los **requerimientos no funcionales (RQNF)** describen validaciones, atributos de calidad (rendimiento, usabilidad, seguridad, fiabilidad, persistencia) y manejo de errores que respaldan a cada funcional.

## Registro de usuario

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF1:** El usuario se registra en el sistema mediante un nombre de usuario, correo electrónico y una contraseña. | **RQNF1:** El sistema valida que el campo de correo electrónico no esté vacío al registrarse.<br>**RQNF2:** El sistema valida que el campo de nombre de usuario no esté vacío al registrarse.<br>**RQNF3:** El sistema valida que el campo de contraseña no esté vacío al registrarse.<br>**RQNF4:** El sistema valida que la contraseña tenga mínimo 4 caracteres.<br>**RQNF5:** El sistema verifica que el correo electrónico no se encuentre registrado previamente.<br>**RQNF6:** El sistema informa con un mensaje claro el motivo del rechazo del registro.<br>**RQNF7 (Seguridad):** El sistema almacena la contraseña en texto plano.<br>**RQNF8:** El sistema captura las excepciones durante el registro y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Inicio de sesión

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF2:** El usuario registrado ingresa su correo electrónico y contraseña para iniciar sesión.<br>**RQF3:** El usuario visualiza un mensaje de error cuando las credenciales no coinciden. | **RQNF9:** El sistema valida que el campo de correo electrónico no esté vacío.<br>**RQNF10:** El sistema verifica que el correo electrónico exista en la base de datos.<br>**RQNF11:** El sistema valida que el campo de contraseña no esté vacío.<br>**RQNF12:** El sistema valida contra la base de datos que las credenciales ingresadas sean correctas.<br>**RQNF13 (Rendimiento):** El sistema resuelve el intento de autenticación en un tiempo no mayor a 2 segundos.<br>**RQNF14:** El sistema captura las excepciones durante el inicio de sesión y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Recuperación de contraseña

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF4:** El usuario accede a la vista de recuperación de contraseña después de tres intentos de inicio de sesión fallidos consecutivos.<br>**RQF5:** El usuario es redirigido a la vista de inicio de sesión después de un cambio de contraseña exitoso. | **RQNF15:** El sistema toma el correo ingresado en la vista de inicio de sesión como la cuenta a modificar.<br>**RQNF16:** El sistema valida que el campo de la nueva contraseña no esté vacío.<br>**RQNF17:** El sistema actualiza la contraseña en la base de datos una vez finalizado el proceso.<br>**RQNF18:** El sistema confirma visualmente al usuario que el cambio de contraseña fue exitoso.<br>**RQNF19:** El sistema captura las excepciones durante el cambio de contraseña y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Registro de eventos de autenticación

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF6:** El sistema registra los intentos fallidos de autenticación y los imprime en el log del servidor. | **RQNF20 (Rendimiento):** El sistema imprime en el log del servidor cada evento de error de autenticación en un tiempo no mayor a 1 segundo desde que se detectan las credenciales incorrectas. |

## Pestaña "Todos"

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF7:** El usuario visualiza en la pestaña "Todos" la lista de todos los usuarios registrados en el sistema. | **RQNF21 (Usabilidad):** El sistema muestra un color particular dependiendo del estado actual de cada usuario.<br>**RQNF22:** El sistema establece el estado de conexión de un usuario a *conectado* cuando inicia sesión.<br>**RQNF23:** El sistema establece el estado de conexión de un usuario a *desconectado* cuando cierra sesión.<br>**RQNF24 (Fiabilidad):** El estado de conexión mostrado refleja el estado real con un desfase no mayor a 2 segundos. |

## Conversaciones en "Chat Todos"

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF8:** El usuario puede seleccionar a otro usuario para iniciar una conversación.<br>**RQF9:** El usuario puede enviar mensajes a usuarios dentro de la vista "Chat Todos".<br>**RQF10:** El usuario puede recibir mensajes desde "Chat Todos". | **RQNF25:** El sistema permite seleccionar únicamente usuarios conectados.<br>**RQNF26:** El sistema abre una ventana de conversación al seleccionar un usuario.<br>**RQNF27:** El sistema valida que el mensaje no esté vacío antes de enviarlo.<br>**RQNF28:** El sistema registra temporalmente el mensaje durante la sesión activa.<br>**RQNF29:** El sistema muestra automáticamente los mensajes recibidos cuando el usuario está dentro de la conversación.<br>**RQNF30 (Rendimiento):** El sistema entrega un mensaje a un destinatario conectado en un tiempo no mayor a 1 segundo.<br>**RQNF31:** El sistema captura las excepciones al enviar un mensaje y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Cierre de sesión

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF11:** El usuario puede cerrar sesión. | **RQNF32:** El sistema elimina el historial de conversación del usuario con los usuarios no amigos.<br>**RQNF33 (Privacidad):** Las conversaciones con usuarios no amigos son efímeras: no se recuperan en sesiones posteriores.<br>**RQNF34 (Fiabilidad):** Tras el cierre de sesión, el estado del usuario pasa a *desconectado* y se propaga a los demás clientes en un tiempo no mayor a 2 segundos. |

## Solicitudes de amistad

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF12:** El usuario envía solicitudes de amistad desde la vista de todos.<br>**RQF13:** El usuario visualiza las invitaciones de amistad enviadas.<br>**RQF14:** El usuario visualiza las invitaciones de amistad recibidas.<br>**RQF15:** El usuario puede aceptar o rechazar invitaciones de amistad recibidas. | **RQNF35:** El sistema registra la solicitud de amistad enviada.<br>**RQNF36 (Disponibilidad):** El sistema notifica al usuario receptor una nueva solicitud en un tiempo no mayor a 2 segundos.<br>**RQNF37:** El sistema notifica que un usuario ya es amigo cuando se intenta enviarle una solicitud.<br>**RQNF38:** El sistema impide enviar solicitudes a usuarios ya agregados como amigos.<br>**RQNF39:** El sistema muestra el estado actual de cada invitación enviada: ACEPTADO, RECHAZADO, PENDIENTE.<br>**RQNF40:** El sistema actualiza el estado de la invitación cuando el destinatario la acepta o rechaza.<br>**RQNF41:** El sistema actualiza la lista de invitaciones recibidas cuando llega una nueva solicitud.<br>**RQNF42:** El sistema muestra el usuario remitente de cada invitación.<br>**RQNF43:** El sistema registra la respuesta seleccionada por el usuario en la base de datos.<br>**RQNF44:** El sistema agrega automáticamente a ambos usuarios a sus respectivas listas de amigos cuando la invitación es aceptada.<br>**RQNF45 (Usabilidad):** El sistema representa los estados (ACEPTADO/RECHAZADO/PENDIENTE) con colores consistentes en todas las vistas.<br>**RQNF46:** El sistema captura las excepciones al gestionar solicitudes y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Pestaña "Amigos"

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF16:** El usuario visualiza la lista de usuarios que aceptaron su solicitud de amistad en la pestaña "Amigos". | **RQNF47:** El sistema muestra únicamente los usuarios con una amistad confirmada.<br>**RQNF48:** El sistema actualiza automáticamente la lista cuando una solicitud de amistad es aceptada.<br>**RQNF49:** El sistema muestra el estado de conexión de cada amigo.<br>**RQNF50 (Usabilidad):** El sistema indica el estado de conexión de cada amigo mediante un indicador visual. |

## Mensajería con amigos

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF17:** El usuario puede enviar mensajes a sus amigos.<br>**RQF18:** El usuario puede recibir mensajes de sus amigos.<br>**RQF19:** El usuario puede visualizar las conversaciones previas de "Chat Amigos" después de iniciar sesión nuevamente. | **RQNF51:** El sistema valida que el mensaje no esté vacío antes de enviarlo.<br>**RQNF52:** El sistema entrega el mensaje al destinatario seleccionado.<br>**RQNF53:** El sistema muestra automáticamente los mensajes recibidos en la conversación correspondiente.<br>**RQNF54:** El sistema almacena los mensajes de "Chat Amigos".<br>**RQNF55:** El sistema recupera las conversaciones almacenadas cuando el usuario vuelve a iniciar sesión.<br>**RQNF56 (Usabilidad):** El sistema muestra el historial de mensajes en orden cronológico.<br>**RQNF57 (Rendimiento):** El sistema entrega un mensaje a un amigo conectado en un tiempo no mayor a 1 segundo.<br>**RQNF58 (Persistencia):** Los mensajes con amigos persisten entre sesiones.<br>**RQNF59:** El sistema captura las excepciones al enviar un mensaje y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Creación y gestión de grupos

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF20:** El usuario puede crear un grupo.<br>**RQF21:** El usuario puede invitar usuarios a un grupo.<br>**RQF22:** El usuario puede visualizar las invitaciones de grupo recibidas.<br>**RQF23:** El usuario puede visualizar las personas invitadas a un grupo y el estado de sus invitaciones.<br>**RQF24:** El usuario puede aceptar o rechazar invitaciones de grupo recibidas.<br>**RQF25:** El usuario puede visualizar los grupos de los que forma parte.<br>**RQF26:** El usuario puede abandonar un grupo en cualquier momento.<br>**RQF27:** El creador de un grupo puede eliminar el grupo.<br>**RQF28:** El creador de un grupo puede abandonar el grupo. | **RQNF60:** El sistema genera un identificador único para el grupo creado.<br>**RQNF61:** El sistema asigna automáticamente el rol de administrador al usuario que creó el grupo.<br>**RQNF62:** El sistema registra la información del grupo.<br>**RQNF63:** El sistema permite enviar invitaciones únicamente a usuarios registrados.<br>**RQNF64:** El sistema registra las invitaciones enviadas para unirse al grupo.<br>**RQNF65 (Disponibilidad):** El sistema notifica a los usuarios una invitación de grupo en un tiempo no mayor a 2 segundos.<br>**RQNF66:** El sistema muestra únicamente las invitaciones asociadas al usuario autenticado.<br>**RQNF67:** El sistema actualiza la lista cuando se recibe una nueva invitación.<br>**RQNF68:** El sistema muestra el nombre del grupo asociado a cada invitación.<br>**RQNF69:** El sistema muestra el estado de cada invitación: ACEPTADO, RECHAZADO, PENDIENTE.<br>**RQNF70:** El sistema actualiza automáticamente el estado de las invitaciones cuando los usuarios responden.<br>**RQNF71:** El sistema muestra únicamente los usuarios invitados al grupo seleccionado.<br>**RQNF72 (Usabilidad):** El sistema representa los estados de invitación con colores consistentes en todas las vistas.<br>**RQNF73:** El sistema registra la respuesta seleccionada por el usuario.<br>**RQNF74:** El sistema agrega automáticamente al usuario como miembro del grupo cuando la invitación es aceptada.<br>**RQNF75:** El sistema actualiza la lista de participantes visibles para los integrantes del grupo.<br>**RQNF76:** El sistema actualiza la lista de grupos cuando el usuario es agregado o eliminado de un grupo.<br>**RQNF77:** El sistema elimina al usuario de la lista de miembros cuando abandona el grupo.<br>**RQNF78:** El sistema actualiza la información de participantes del grupo después de la salida de un usuario.<br>**RQNF79 (Permanencia):** El grupo permanece activo únicamente si cuenta con al menos tres miembros con invitación ACEPTADA, incluido el creador.<br>**RQNF80 (Permanencia):** El sistema elimina el grupo cuando no se alcanza el mínimo de tres miembros aceptados, aunque existan invitaciones pendientes, y elimina también las invitaciones asociadas.<br>**RQNF81 (Autorización):** Únicamente el creador del grupo puede eliminarlo.<br>**RQNF82:** El sistema elimina el grupo seleccionado de forma permanente.<br>**RQNF83:** El sistema elimina las invitaciones asociadas al grupo eliminado.<br>**RQNF84:** El sistema notifica a los integrantes que el grupo ha sido eliminado.<br>**RQNF85:** El sistema elimina automáticamente el grupo cuando el creador lo abandona.<br>**RQNF86:** El sistema elimina los registros asociados al grupo una vez que éste deja de existir.<br>**RQNF87 (Consistencia):** El sistema evalúa las reglas de permanencia de forma atómica tras cada cambio de membresía.<br>**RQNF88:** El sistema captura las excepciones en las operaciones de grupo y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Mensajería en grupos

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF29:** El usuario puede visualizar el historial de mensajes de un grupo al ingresar.<br>**RQF30:** El usuario puede enviar mensajes dentro de un grupo.<br>**RQF31:** El usuario puede recibir mensajes dentro de un grupo. | **RQNF89:** El sistema recupera los mensajes almacenados del grupo seleccionado.<br>**RQNF90:** El sistema muestra el historial completo disponible para los integrantes del grupo.<br>**RQNF91 (Usabilidad):** El sistema muestra los mensajes en orden cronológico.<br>**RQNF92:** El sistema valida que el mensaje no esté vacío antes de enviarlo.<br>**RQNF93:** El sistema distribuye el mensaje a todos los integrantes activos del grupo.<br>**RQNF94:** El sistema almacena el mensaje en el historial del grupo.<br>**RQNF95:** El sistema muestra automáticamente los mensajes recibidos.<br>**RQNF96:** El sistema identifica al remitente de cada mensaje.<br>**RQNF97 (Rendimiento):** El sistema actualiza la conversación grupal con los nuevos mensajes en un tiempo no mayor a 2 segundos.<br>**RQNF98 (Persistencia):** El historial de mensajes del grupo persiste entre sesiones.<br>**RQNF99:** El sistema captura las excepciones al enviar un mensaje y muestra un mensaje de error en lugar de interrumpir su ejecución. |

## Administración del servidor

| Requerimiento Funcional | Requerimientos No Funcionales asociados |
| --- | --- |
| **RQF32:** El administrador del sistema puede visualizar las acciones realizadas por el servidor. | **RQNF100:** El sistema registra los eventos generados por los usuarios.<br>**RQNF101:** El sistema muestra los eventos registrados.<br>**RQNF102:** El sistema actualiza la información mostrada conforme ocurren nuevas acciones.<br>**RQNF103 (Rendimiento):** El sistema refleja los nuevos eventos en la vista del administrador en un tiempo no mayor a 1 segundo.<br>**RQNF104 (Usabilidad):** El sistema muestra cada evento de forma legible e incluye su marca temporal. |
