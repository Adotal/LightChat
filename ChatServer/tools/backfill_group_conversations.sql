-- ============================================================
-- Backfill: reconstruir conversaciones y miembros de grupos que
-- quedaron huérfanos.
--
-- Los grupos creados ANTES de arreglar el esquema de `conversations`
-- (columna `last_seen` y ENUM `type`) nunca obtuvieron su fila en
-- `conversations`, por lo que `conversation_members` quedó vacía y
-- `handleGroupMessage` distribuía a 0 miembros (nadie recibía los
-- mensajes de grupo).
--
-- Idempotente: todos los INSERT usan NOT EXISTS, se puede ejecutar
-- varias veces sin duplicar.
-- ============================================================

-- 1) Crear la conversación GROUP que falta para cada grupo existente.
INSERT INTO conversations (id_group, type, last_seen)
SELECT g.id_group, 'GROUP', NOW()
FROM chat_groups g
WHERE NOT EXISTS (
    SELECT 1 FROM conversations c
    WHERE c.id_group = g.id_group AND c.type = 'GROUP'
);

-- 2) Registrar a cada propietario como miembro de su conversación de grupo.
INSERT INTO conversation_members (id_user, id_conversation)
SELECT g.id_owner, c.id_conversation
FROM chat_groups g
JOIN conversations c ON c.id_group = g.id_group AND c.type = 'GROUP'
WHERE NOT EXISTS (
    SELECT 1 FROM conversation_members cm
    WHERE cm.id_user = g.id_owner AND cm.id_conversation = c.id_conversation
);

-- 3) Registrar a cada invitado APPROVED como miembro de la conversación de grupo.
INSERT INTO conversation_members (id_user, id_conversation)
SELECT gi.id_invited, c.id_conversation
FROM group_invitations gi
JOIN conversations c ON c.id_group = gi.id_group AND c.type = 'GROUP'
WHERE gi.status = 'APPROVED'
  AND NOT EXISTS (
      SELECT 1 FROM conversation_members cm
      WHERE cm.id_user = gi.id_invited AND cm.id_conversation = c.id_conversation
  );
