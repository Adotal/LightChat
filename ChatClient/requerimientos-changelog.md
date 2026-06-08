# Changelog de la revisión de requerimientos

Revisión de redacción, precisión y clasificación de [requerimientos.md](requerimientos.md).

## Criterio final (v3)

- **Las validaciones son requerimientos NO funcionales.**
- **Lo que el usuario percibe es funcional:** las confirmaciones y los mensajes de error que el usuario ve (incluido el manejo de fallos por try/catch) son requerimientos funcionales, redactados desde la perspectiva del usuario ("El usuario visualiza...").
- En la columna no funcional quedan solo validaciones internas y atributos de calidad (rendimiento, usabilidad, seguridad, fiabilidad, persistencia).
- **Numeración plana:** `RQF1..42` y `RQNF1..94`, continua y sin sub-numeración (`x.x`).

## Cambio v2 → v3

Los mensajes/confirmaciones que el usuario percibe se movieron de la columna no funcional a la funcional, redactados desde la perspectiva del usuario:

- Mensaje de motivo de rechazo del registro (ex-RQNF6) → **RQF2**.
- Mensajes de error por try/catch (ex-RQNF8, 14, 19, 31, 46, 59, 88, 99) → **RQF3, RQF6, RQF10, RQF16, RQF22, RQF27, RQF37, RQF41**.
- Confirmación de cambio de contraseña (ex-RQNF18) → **RQF9**.

Todo el documento se renumeró en consecuencia (RQF1–42, RQNF1–94).

## Correcciones aplicadas

| # | Cambio |
| --- | --- |
| 1 | Estructura aplanada: se eliminaron los sub-requerimientos `RQFx.y`; las validaciones volvieron a la columna no funcional. |
| 2 | **Contraseña:** se documenta explícitamente como **RQNF7 (Seguridad): el sistema almacena la contraseña en texto plano.** Se eliminaron los RQNF de hash/cifrado. |
| 3 | **Manejo de errores (try/catch):** se agregaron RQNF específicos en operaciones críticas — registro (RQNF8), login (RQNF14), recuperación (RQNF19), Chat Todos (RQNF31), solicitudes de amistad (RQNF46), mensajería amigos (RQNF59), grupos (RQNF88), mensajería en grupos (RQNF99). |
| 4 | **RQNF45** reescrito: "El sistema representa los estados (ACEPTADO/RECHAZADO/PENDIENTE) con colores consistentes en todas las vistas." (era el antiguo RQNF17/RQNF29). |
| 5 | **RQF3 + RQF7 originales** (duplicados de "mensaje de error de credenciales") combinados en **RQF3**. |
| 6 | **RQNF13 original** ("contraseña no vacía al cambiar contraseña"), que estaba mal ubicado bajo el login, se movió a Recuperación de contraseña (**RQNF16**). |
| 7 | **RQF6 / RQF34 originales** (registro de eventos vs. visualización del admin) se mantienen separados: **RQF6** (log) y **RQF32** (admin). |
| 8 | Validaciones repetidas entre secciones (p. ej. "mensaje no vacío") se mantienen por sección, para que cada feature sea autocontenida. |

## Corrección de las reglas de permanencia de grupo

- **Eliminada** la condición incorrecta "el sistema mantiene activo el grupo mientras exista al menos una invitación PENDIENTE" (esa condición no existe).
- **RQNF79:** el grupo permanece activo únicamente si cuenta con **al menos tres miembros con invitación ACEPTADA, incluido el creador**.
- **RQNF80 (corregido):** el sistema elimina el grupo cuando no se alcanza el mínimo de tres miembros aceptados, **aunque existan invitaciones pendientes**, y **elimina también las invitaciones asociadas**.

## Verificación

- ✅ Numeración plana y continua (RQF1–32, RQNF1–104); sin `x.x`.
- ✅ Validaciones clasificadas como no funcionales.
- ✅ Cada RQF tiene al menos un RQNF asociado.
- ✅ RQNF de texto plano presente; sin RQNF de hash.
- ✅ RQNF de manejo de errores en las operaciones críticas.
- ✅ Reglas de grupo corregidas.
