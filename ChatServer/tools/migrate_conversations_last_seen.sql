-- ============================================================
-- Migración: alinear la tabla `conversations` desplegada con el
-- esquema canónico (ChatServer/schema.sql).
--
-- La tabla desplegada se creó sin la columna `last_seen`, por lo
-- que TODO INSERT/UPDATE de conversaciones fallaba con
-- "Unknown column 'last_seen' in 'field list'". Esto impedía crear
-- conversaciones (directas y de grupo), y en cascada fallaban
-- conversation_members y messages por restricción de clave foránea:
-- los mensajes de grupo no llegaban a nadie y los directos no se
-- persistían.
--
-- Ejecutar una sola vez. Si la columna ya existe, el ALTER fallará
-- por columna duplicada (inofensivo).
-- ============================================================

ALTER TABLE conversations
    ADD COLUMN last_seen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
