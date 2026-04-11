-- ============================================
-- V4__matching_interests.sql
-- AgraBandhan - Partner Preferences, Interests,
-- Daily Matches
-- ============================================

-- Partner preferences (what the user wants in a match)
CREATE TABLE partner_preferences (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,

    -- Age
    age_min                 INTEGER         DEFAULT 18,
    age_max                 INTEGER         DEFAULT 40,

    -- Height (cm)
    height_min              INTEGER,
    height_max              INTEGER,

    -- Marital status
    preferred_marital_statuses VARCHAR(255),

    -- Education
    min_qualification       VARCHAR(50),

    -- Profession
    preferred_employed_in   VARCHAR(255),
    min_income_range        VARCHAR(50),

    -- Location
    preferred_cities        VARCHAR(500),
    preferred_states        VARCHAR(255),
    preferred_country       VARCHAR(50)     DEFAULT 'India',

    -- Lifestyle
    preferred_diet          VARCHAR(20),
    smoking_acceptable      BOOLEAN         DEFAULT FALSE,
    drinking_acceptable     BOOLEAN         DEFAULT FALSE,

    -- Family
    preferred_family_type   VARCHAR(20),

    -- Gotra exclusions (beyond auto same-gotra exclusion)
    exclude_gotras          VARCHAR(255),

    -- Manglik preference
    manglik_preference      VARCHAR(20)     CHECK (manglik_preference IN ('YES', 'NO', 'DOESNT_MATTER')),

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_partner_pref_profile ON partner_preferences(profile_id);

-- Interests (express interest in a profile)
CREATE TABLE interests (
    id                      BIGSERIAL       PRIMARY KEY,
    sender_profile_id       BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    receiver_profile_id     BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'WITHDRAWN')),

    message                 TEXT,
    decline_reason          VARCHAR(255),

    sent_at                 TIMESTAMP       NOT NULL DEFAULT NOW(),
    responded_at            TIMESTAMP,

    CONSTRAINT uq_interest UNIQUE (sender_profile_id, receiver_profile_id)
);

CREATE INDEX idx_interests_sender ON interests(sender_profile_id, status);
CREATE INDEX idx_interests_receiver ON interests(receiver_profile_id, status);
CREATE INDEX idx_interests_status ON interests(status);

-- Daily matches (pre-computed by scheduled job)
CREATE TABLE daily_matches (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    matched_profile_id      BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    compatibility_score     INTEGER         NOT NULL DEFAULT 0,
    match_date              DATE            NOT NULL DEFAULT CURRENT_DATE,

    -- Score breakdown
    family_score            INTEGER         DEFAULT 0,
    education_score         INTEGER         DEFAULT 0,
    location_score          INTEGER         DEFAULT 0,
    lifestyle_score         INTEGER         DEFAULT 0,
    preference_score        INTEGER         DEFAULT 0,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_daily_match UNIQUE (profile_id, matched_profile_id, match_date)
);

CREATE INDEX idx_daily_matches_profile_date ON daily_matches(profile_id, match_date DESC);
CREATE INDEX idx_daily_matches_score ON daily_matches(compatibility_score DESC);
