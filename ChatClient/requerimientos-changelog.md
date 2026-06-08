# Changelog de la revisión de requerimientos

Revisión de redacción, precisión y **reclasificación funcional / no funcional** del documento [requerimientos.md](requerimientos.md). Criterio aplicado: rigor académico estricto.

## 1. Hallazgo principal

De los 85 "RQNF" originales, **la gran mayoría no eran no funcionales**: describían comportamientos del sistema (validar, registrar, notificar, actualizar, eliminar), que es lógica **funcional**. Un no funcional describe un *atributo de calidad medible*, no una acción.

**Acción:** esos comportamientos se promovieron a **sub-requerimientos funcionales** (`RQFx.y`) y se redactaron **31 RQNF genuinos**, repartidos en categorías de calidad: Rendimiento, Usabilidad, Seguridad, Fiabilidad/Consistencia, Disponibilidad/Persistencia/Privacidad.

## 2. Errores puntuales corregidos

| # | Problema original | Corrección |
| --- | --- | --- |
| 1 | **RQNF13** ("valida contraseña no vacía al cambiar contraseña") estaba bajo **RQF7** (mensaje de error en login). | Movido a *Recuperación de contraseña* como **RQF3.2**. |
| 2 | **RQF3** y **RQF7** eran casi duplicados ("mensaje de error si las credenciales no coinciden"). | Consolidados en **RQF2.5** (un único sub-requerimiento de login). |
| 3 | **RQF6** (registra intentos fallidos en log) se solapaba con **RQF34** + RQNF83-85 (admin visualiza acciones). | Separados: **RQF4** = registro de eventos de autenticación; **RQF31** = visualización por el administrador. |
| 4 | Numeración con huecos tras reclasificar. | Renumeración continua: **RQF1–RQF31** y **RQNF1–RQNF31**. |
| 5 | **RQF22/RQF24/RQF25** (reglas de permanencia, RQNF22-24, RQNF24) mezclaban acción del sistema con "el usuario debe poder formar parte". | Reescrito **RQF22** como acción del sistema ("aplica las condiciones mínimas de permanencia"). |

## 3. NFR genuinos generados (resumen por categoría)

- **Seguridad:** hash de contraseña en registro/login/recuperación (RQNF1, RQNF3, RQNF6); autorización: solo el creador elimina grupo (RQNF24); mensaje de error que no revela el campo fallido (RQNF5).
- **Rendimiento:** autenticación ≤2 s (RQNF4); log de eventos ≤1 s (RQNF8); entrega de mensajes ≤1 s (RQNF11, RQNF20); actualización grupal ≤2 s (RQNF28); vista de admin ≤1 s (RQNF30).
- **Usabilidad:** color por estado (RQNF9, RQNF18); orden cronológico (RQNF12, RQNF22, RQNF27); estados con etiqueta/color consistentes (RQNF17, RQNF26); confirmación de cambio (RQNF7); legibilidad + marca temporal (RQNF31).
- **Fiabilidad/Consistencia/Disponibilidad:** desfase de estado ≤2 s (RQNF10, RQNF14, RQNF19); consistencia de invitaciones (RQNF16); reglas de grupo atómicas (RQNF23); notificación ≤2 s (RQNF15, RQNF25).
- **Persistencia/Privacidad:** mensajes de amigos y grupos persisten entre sesiones (RQNF21, RQNF29); conversaciones con no amigos son efímeras (RQNF13).

## 4. Mapeo de numeración (vieja → nueva)

| Original | Nuevo |
| --- | --- |
| RQF1 | RQF1 |
| RQNF1–RQNF5 (validaciones registro) | RQF1.1–RQF1.5 |
| RQF2 | RQF2 |
| RQNF6, RQNF7, RQNF8, RQNF9 | RQF2.1, RQF2.2, RQF2.3, RQF2.4 |
| RQF3 (duplicado) + RQF7 | RQF2.5 |
| RQF4, RQNF10 | RQF3, RQF3.1 |
| RQNF13 (mal ubicado) | RQF3.2 |
| RQF5, RQNF11 | RQF3.4, RQF3.3 |
| RQF6, RQNF12 | RQF4, RQNF8 |
| RQF8, RQNF14, RQNF15, RQNF16 | RQF5, RQNF9, RQF5.1, RQF5.2 |
| RQF9–RQF11, RQNF17–RQNF21 | RQF6–RQF8 (+ sub) |
| RQF12, RQNF22–RQNF24 | RQF9, RQF9.1 (+ RQNF13/14) |
| RQF13–RQF16, RQNF25–RQNF35 | RQF10–RQF13 (+ sub) |
| RQF17, RQNF36–RQNF38 | RQF14 (+ sub) |
| RQF18–RQF20, RQNF39–RQNF45 | RQF15–RQF17 (+ sub) |
| RQF21–RQF30, RQNF46–RQNF73 | RQF18–RQF27 (+ sub) |
| RQF31–RQF33, RQNF74–RQNF82 | RQF28–RQF30 (+ sub) |
| RQF34, RQNF83–RQNF85 | RQF31 (+ sub) |

## 5. Verificación

- ✅ Cada RQF (31) tiene al menos un RQNF genuino asociado.
- ✅ Cada RQNF (31) es medible/verificable y pertenece a una categoría de calidad.
- ✅ Numeración continua sin huecos ni duplicados.
- ✅ Ningún comportamiento funcional original se perdió (todos los ex-RQNF de comportamiento aparecen como sub-funcionales).
