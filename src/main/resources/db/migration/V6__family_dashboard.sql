-- ============================================
-- V6__family_dashboard.sql
-- AgraBandhan - Family Members, Activity Log,
-- Voice Introductions
-- ============================================

-- Family members (up to 4 per profile, each with own login)
CREATE TABLE family_members (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    user_id                 BIGINT          REFERENCES users(id) ON DELETE SET NULL,

    name                    VARCHAR(100)    NOT NULL,
    relationship            VARCHAR(20)     NOT NULL
                            CHECK (relationship IN ('FATHER', 'MOTHER', 'BROTHER', 'SISTER', 'SELF')),
    phone_number            VARCHAR(15),

    -- Permissions
    can_search              BOOLEAN         NOT NULL DEFAULT TRUE,
    can_shortlist           BOOLEAN         NOT NULL DEFAULT TRUE,
    can_send_interest       BOOLEAN         NOT NULL DEFAULT FALSE,
    can_accept_interest     BOOLEAN         NOT NULL DEFAULT FALSE,
    can_chat                BOOLEAN         NOT NULL DEFAULT FALSE,
    can_edit_profile        BOOLEAN         NOT NULL DEFAULT FALSE,

    invite_token            VARCHAR(100)    UNIQUE,
    invite_status           VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                            CHECK (invite_status IN ('PENDING', 'ACCEPTED', 'EXPIRED')),
    invite_expires_at       TIMESTAMP,

    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_family_members_profile ON family_members(profile_id);
CREATE INDEX idx_family_members_user ON family_members(user_id);
CREATE INDEX idx_family_members_invite ON family_members(invite_token);

-- Ensure max 4 family members per profile
CREATE OR REPLACE FUNCTION check_family_member_limit()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM family_members WHERE profile_id = NEW.profile_id AND is_active = true) >= 4 THEN
        RAISE EXCEPTION 'Maximum 4 family members allowed per profile';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_family_member_limit
    BEFORE INSERT ON family_members
    FOR EACH ROW EXECUTE FUNCTION check_family_member_limit();

-- Activity log (audit trail of all actions on a profile)
CREATE TABLE profile_activity_log (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    actor_user_id           BIGINT          NOT NULL REFERENCES users(id),
    actor_name              VARCHAR(100)    NOT NULL,
    actor_relationship      VARCHAR(20),

    action_type             VARCHAR(50)     NOT NULL
                            CHECK (action_type IN (
                                'PROFILE_UPDATED', 'PHOTO_UPLOADED', 'PHOTO_DELETED',
                                'INTEREST_SENT', 'INTEREST_ACCEPTED', 'INTEREST_DECLINED',
                                'SEARCH_PERFORMED', 'PROFILE_SHORTLISTED',
                                'FAMILY_MEMBER_ADDED', 'FAMILY_MEMBER_REMOVED',
                                'CONTACT_SHARED', 'MESSAGE_SENT',
                                'PREFERENCE_UPDATED', 'VOICE_INTRO_UPLOADED'
                            )),
    action_detail           TEXT,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_log_profile ON profile_activity_log(profile_id, created_at DESC);
CREATE INDEX idx_activity_log_actor ON profile_activity_log(actor_user_id);

-- Voice introductions
CREATE TABLE voice_introductions (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,
    recorded_by_user_id     BIGINT          NOT NULL REFERENCES users(id),
    recorded_by_name        VARCHAR(100)    NOT NULL,
    recorded_by_relationship VARCHAR(20),

    audio_key               VARCHAR(500)    NOT NULL,
    duration_seconds        INTEGER         NOT NULL,
    file_size_bytes         BIGINT,

    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_voice_intro_profile ON voice_introductions(profile_id);
