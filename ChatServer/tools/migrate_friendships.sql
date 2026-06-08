-- ============================================================
-- Migración: alinear la tabla `friendships` desplegada con el
-- esquema canónico (ChatServer/schema.sql).
--
-- La tabla desplegada quedó con columnas/enum distintos a los que
-- usa el código (id_addressee, date, state[accepted/pending/denied]),
-- por lo que TODAS las consultas de amistad fallaban con
-- "Unknown column 'id_receiver'". Esta migración renombra columnas y
-- convierte el enum a los nombres/valores canónicos
-- (id_receiver, created_at, status[PENDING/APPROVED/DENIED]).
--
-- Idempotente en la práctica: ejecutar una sola vez. Si ya está
-- migrada, los CHANGE COLUMN fallarán por columna inexistente.
-- ============================================================

-- 1. Renombrar id_addressee -> id_receiver
ALTER TABLE friendships
    CHANGE COLUMN id_addressee id_receiver INT NOT NULL;

-- 2. Renombrar date -> created_at (sin ON UPDATE)
ALTER TABLE friendships
    CHANGE COLUMN date created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 3. Convertir state (minúsculas) -> status (canónico)
ALTER TABLE friendships
    ADD COLUMN status ENUM('PENDING','APPROVED','DENIED') NOT NULL DEFAULT 'PENDING';

UPDATE friendships SET status = CASE state
    WHEN 'accepted' THEN 'APPROVED'
    WHEN 'pending'  THEN 'PENDING'
    WHEN 'denied'   THEN 'DENIED'
    ELSE 'PENDING'
END;

ALTER TABLE friendships
    DROP COLUMN state;
