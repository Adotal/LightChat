-- ============================================================
-- Migración: alinear el ENUM de `conversations.type` con el
-- esquema canónico (ChatServer/schema.sql:52).
--
-- La tabla desplegada tenía el ENUM `type` con valores distintos a
-- los que usa el código ('TEMP','FRIEND','GROUP'), provocando
-- "Data truncated for column 'type' at row 1" en cada INSERT de
-- conversación y, en cascada, fallos de clave foránea en
-- conversation_members y messages.
--
-- Ejecutar una sola vez.
-- ============================================================

ALTER TABLE conversations
    MODIFY COLUMN type ENUM('TEMP','FRIEND','GROUP') NOT NULL;
