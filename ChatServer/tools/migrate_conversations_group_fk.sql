-- ============================================================
-- Migración: corregir la FK de `conversations.id_group`.
--
-- La BD desplegada arrastra tablas de un esquema antiguo (`groups`,
-- `users_groups`, `users_conversations`). La FK `fk_group_c` de
-- `conversations.id_group` apuntaba a la tabla vieja `groups` (vacía),
-- mientras que la aplicación inserta los grupos en `chat_groups`. Por
-- eso TODO INSERT de conversación de grupo fallaba por restricción de
-- clave foránea, aun con `last_seen` y el ENUM `type` ya corregidos.
--
-- Se reapunta la FK a `chat_groups`, alineándola con el esquema
-- canónico (schema.sql: fk_conv_group).
--
-- Ejecutar una sola vez.
-- ============================================================

ALTER TABLE conversations DROP FOREIGN KEY fk_group_c;

ALTER TABLE conversations
    ADD CONSTRAINT fk_conv_group FOREIGN KEY (id_group)
    REFERENCES chat_groups(id_group) ON DELETE CASCADE;
