-- ============================================
-- V5__chat_system.sql
-- AgraBandhan - In-App Chat, Conversations,
-- Contact Sharing
-- ============================================

-- Conversations (created when interest is accepted)
CREATE TABLE conversations (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_a_id            BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    profile_b_id            BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    last_message_at         TIMESTAMP,
    last_message_preview    VARCHAR(255),

    -- Contact sharing status per side
    profile_a_shared_contact BOOLEAN        NOT NULL DEFAULT FALSE,
    profile_b_shared_contact BOOLEAN        NOT NULL DEFAULT FALSE,

    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_conversation UNIQUE (profile_a_id, profile_b_id),
    CONSTRAINT chk_different_profiles CHECK (profile_a_id < profile_b_id)
);

CREATE INDEX idx_conversations_profile_a ON conversations(profile_a_id, last_message_at DESC);
CREATE INDEX idx_conversations_profile_b ON conversations(profile_b_id, last_message_at DESC);

-- Chat messages
CREATE TABLE chat_messages (
    id                      BIGSERIAL       PRIMARY KEY,
    conversation_id         BIGINT          NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_profile_id       BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    content                 TEXT            NOT NULL,
    message_type            VARCHAR(20)     NOT NULL DEFAULT 'TEXT'
                            CHECK (message_type IN ('TEXT', 'CONTACT_SHARED', 'SYSTEM')),

    is_read                 BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at                 TIMESTAMP,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages(conversation_id, created_at DESC);
CREATE INDEX idx_chat_messages_unread ON chat_messages(conversation_id, is_read) WHERE is_read = FALSE;
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_profile_id);

-- Block list (blocks chat + all interactions)
CREATE TABLE blocked_profiles (
    id                      BIGSERIAL       PRIMARY KEY,
    blocker_profile_id      BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    blocked_profile_id      BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    reason                  VARCHAR(255),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_block UNIQUE (blocker_profile_id, blocked_profile_id)
);

CREATE INDEX idx_blocked_blocker ON blocked_profiles(blocker_profile_id);
CREATE INDEX idx_blocked_blocked ON blocked_profiles(blocked_profile_id);

-- Reports (for moderation)
CREATE TABLE profile_reports (
    id                      BIGSERIAL       PRIMARY KEY,
    reporter_profile_id     BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    reported_profile_id     BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    reason                  VARCHAR(50)     NOT NULL
                            CHECK (reason IN ('FAKE_PROFILE', 'INAPPROPRIATE_PHOTO', 'HARASSMENT',
                                              'SPAM', 'WRONG_COMMUNITY', 'OTHER')),
    description             TEXT,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'REVIEWED', 'ACTION_TAKEN', 'DISMISSED')),
    reviewed_by             BIGINT          REFERENCES users(id),
    reviewed_at             TIMESTAMP,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_report UNIQUE (reporter_profile_id, reported_profile_id)
);

CREATE INDEX idx_reports_status ON profile_reports(status);
CREATE INDEX idx_reports_reported ON profile_reports(reported_profile_id);
