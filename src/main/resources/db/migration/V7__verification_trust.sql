-- ============================================
-- V7__verification_trust.sql
-- AgraBandhan - ID Verification, Trust Score,
-- Community Endorsements, Samaj Directory
-- ============================================

-- ID Verification requests
CREATE TABLE verification_requests (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    document_type           VARCHAR(20)     NOT NULL
                            CHECK (document_type IN ('AADHAAR', 'PAN', 'VOTER_ID', 'PASSPORT', 'DRIVING_LICENSE')),
    document_key            VARCHAR(500)    NOT NULL,
    selfie_key              VARCHAR(500),

    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason        VARCHAR(255),

    reviewed_by             BIGINT          REFERENCES users(id),
    reviewed_at             TIMESTAMP,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verification_profile ON verification_requests(profile_id);
CREATE INDEX idx_verification_status ON verification_requests(status);

-- Photo verification
CREATE TABLE photo_verifications (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    selfie_key              VARCHAR(500)    NOT NULL,

    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason        VARCHAR(255),

    reviewed_by             BIGINT          REFERENCES users(id),
    reviewed_at             TIMESTAMP,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_photo_verify_profile ON photo_verifications(profile_id);
CREATE INDEX idx_photo_verify_status ON photo_verifications(status);

-- Community endorsements
CREATE TABLE community_endorsements (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    endorser_profile_id     BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    endorsement_text        TEXT            NOT NULL,
    relationship            VARCHAR(50),

    is_approved             BOOLEAN         NOT NULL DEFAULT FALSE,
    approved_at             TIMESTAMP,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_endorsement UNIQUE (profile_id, endorser_profile_id)
);

CREATE INDEX idx_endorsement_profile ON community_endorsements(profile_id);
CREATE INDEX idx_endorsement_endorser ON community_endorsements(endorser_profile_id);

-- Samaj directory (local Bisa Aggarwal Sabhas)
CREATE TABLE samaj_sabhas (
    id                      BIGSERIAL       PRIMARY KEY,
    name                    VARCHAR(200)    NOT NULL,
    city                    VARCHAR(100)    NOT NULL,
    state                   VARCHAR(50)     NOT NULL,
    address                 TEXT,
    contact_person          VARCHAR(100),
    contact_phone           VARCHAR(15),
    contact_email           VARCHAR(100),

    member_count            INTEGER         DEFAULT 0,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sabha_city ON samaj_sabhas(city);
CREATE INDEX idx_sabha_state ON samaj_sabhas(state);

-- Profile to Sabha link
CREATE TABLE profile_sabha_links (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    sabha_id                BIGINT          NOT NULL REFERENCES samaj_sabhas(id) ON DELETE CASCADE,
    verified_by_leader      BOOLEAN         NOT NULL DEFAULT FALSE,
    verified_at             TIMESTAMP,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_profile_sabha UNIQUE (profile_id, sabha_id)
);

CREATE INDEX idx_profile_sabha_profile ON profile_sabha_links(profile_id);
CREATE INDEX idx_profile_sabha_sabha ON profile_sabha_links(sabha_id);

-- Trust score components (stored for transparency)
CREATE TABLE trust_scores (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,

    total_score             INTEGER         NOT NULL DEFAULT 0,

    -- Breakdown
    id_verification_score   INTEGER         NOT NULL DEFAULT 0,
    photo_verification_score INTEGER        NOT NULL DEFAULT 0,
    endorsement_score       INTEGER         NOT NULL DEFAULT 0,
    sabha_verification_score INTEGER        NOT NULL DEFAULT 0,
    completeness_score      INTEGER         NOT NULL DEFAULT 0,
    response_rate_score     INTEGER         NOT NULL DEFAULT 0,

    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trust_score_profile ON trust_scores(profile_id);
CREATE INDEX idx_trust_score_total ON trust_scores(total_score DESC);
