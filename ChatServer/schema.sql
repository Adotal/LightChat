-- ============================================================
-- Esquema canónico LightChat (fuente de verdad)
-- Todas las tablas y columnas en inglés. Aplicar con:
--   mysql -h <host> -P <port> -u <user> -p <db> < schema.sql
-- Las tablas usan IF NOT EXISTS para ser idempotentes.
-- ============================================================

-- Usuarios -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id_user      INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    is_connected BOOLEAN      NOT NULL DEFAULT FALSE,
    last_access  TIMESTAMP    NULL DEFAULT NULL
);

-- Amistades / solicitudes de amistad --------------------------
CREATE TABLE IF NOT EXISTS friendships (
    id_friendship INT AUTO_INCREMENT PRIMARY KEY,
    id_sender     INT NOT NULL,
    id_receiver   INT NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        ENUM('PENDING','APPROVED','DENIED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_friend_sender   FOREIGN KEY (id_sender)   REFERENCES users(id_user) ON DELETE CASCADE,
    CONSTRAINT fk_friend_receiver FOREIGN KEY (id_receiver) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Grupos (groups es palabra reservada en MySQL 8 -> chat_groups)
CREATE TABLE IF NOT EXISTS chat_groups (
    id_group   INT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(150) NOT NULL,
    id_owner   INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_owner FOREIGN KEY (id_owner) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Invitaciones a grupo ----------------------------------------
CREATE TABLE IF NOT EXISTS group_invitations (
    id_invitation INT AUTO_INCREMENT PRIMARY KEY,
    id_group      INT NOT NULL,
    id_invited    INT NOT NULL,
    status        ENUM('PENDING','APPROVED','DENIED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_inv_group   FOREIGN KEY (id_group)   REFERENCES chat_groups(id_group) ON DELETE CASCADE,
    CONSTRAINT fk_inv_invited FOREIGN KEY (id_invited) REFERENCES users(id_user)        ON DELETE CASCADE
);

-- Conversaciones (directas TEMP/FRIEND o de grupo GROUP) ------
CREATE TABLE IF NOT EXISTS conversations (
    id_conversation INT AUTO_INCREMENT PRIMARY KEY,
    id_group        INT NULL,
    type            ENUM('TEMP','FRIEND','GROUP') NOT NULL,
    last_seen       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_group FOREIGN KEY (id_group) REFERENCES chat_groups(id_group) ON DELETE CASCADE
);

-- Miembros de cada conversación -------------------------------
CREATE TABLE IF NOT EXISTS conversation_members (
    id_user         INT NOT NULL,
    id_conversation INT NOT NULL,
    PRIMARY KEY (id_user, id_conversation),
    CONSTRAINT fk_member_user FOREIGN KEY (id_user)         REFERENCES users(id_user)               ON DELETE CASCADE,
    CONSTRAINT fk_member_conv FOREIGN KEY (id_conversation) REFERENCES conversations(id_conversation) ON DELETE CASCADE
);

-- Mensajes -----------------------------------------------------
CREATE TABLE IF NOT EXISTS messages (
    id_message      INT AUTO_INCREMENT PRIMARY KEY,
    id_conversation INT NOT NULL,
    id_sender       INT NOT NULL,
    content         TEXT NOT NULL,
    sent_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_conv   FOREIGN KEY (id_conversation) REFERENCES conversations(id_conversation) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY (id_sender)       REFERENCES users(id_user)                 ON DELETE CASCADE
);

-- Eventos del servidor (log de administrador) -----------------
CREATE TABLE IF NOT EXISTS server_events (
    id_event    INT AUTO_INCREMENT PRIMARY KEY,
    type        VARCHAR(50)  NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
