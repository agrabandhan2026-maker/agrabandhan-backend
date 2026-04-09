-- ============================================
-- V2__create_profile_tables.sql
-- AgraBandhan - Profile, Family, Education,
-- Profession, Lifestyle, Photos
-- ============================================

-- Profile (core personal details, 1:1 with users)
CREATE TABLE profiles (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    -- Personal
    first_name          VARCHAR(50)     NOT NULL,
    last_name           VARCHAR(50)     NOT NULL,
    gender              VARCHAR(10)     NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    date_of_birth       DATE            NOT NULL,
    height_cm           INTEGER,
    weight_kg           INTEGER,
    complexion          VARCHAR(20),
    blood_group         VARCHAR(5),
    marital_status      VARCHAR(20)     NOT NULL DEFAULT 'NEVER_MARRIED'
                        CHECK (marital_status IN ('NEVER_MARRIED', 'DIVORCED', 'WIDOWED', 'AWAITING_DIVORCE')),
    disability          VARCHAR(100),

    -- Community
    gotra               VARCHAR(20)     NOT NULL,
    sub_caste           VARCHAR(50),
    mother_gotra        VARCHAR(20),

    -- Location
    current_city        VARCHAR(100),
    current_state       VARCHAR(50),
    current_country     VARCHAR(50)     DEFAULT 'India',
    native_city         VARCHAR(100),
    native_state        VARCHAR(50),

    -- About
    about_me            TEXT,
    profile_managed_by  VARCHAR(20)     DEFAULT 'SELF'
                        CHECK (profile_managed_by IN ('SELF', 'PARENT', 'SIBLING', 'RELATIVE', 'FRIEND')),

    -- Completeness
    completeness_score  INTEGER         NOT NULL DEFAULT 0,

    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_user_id ON profiles(user_id);
CREATE INDEX idx_profiles_gender ON profiles(gender);
CREATE INDEX idx_profiles_gotra ON profiles(gotra);
CREATE INDEX idx_profiles_current_city ON profiles(current_city);
CREATE INDEX idx_profiles_dob ON profiles(date_of_birth);
CREATE INDEX idx_profiles_marital_status ON profiles(marital_status);

-- Family details
CREATE TABLE family_details (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,

    father_name             VARCHAR(100),
    father_occupation       VARCHAR(100),
    mother_name             VARCHAR(100),
    mother_occupation       VARCHAR(100),

    family_type             VARCHAR(20)     CHECK (family_type IN ('JOINT', 'NUCLEAR', 'OTHER')),
    family_values           VARCHAR(20)     CHECK (family_values IN ('TRADITIONAL', 'MODERATE', 'LIBERAL')),
    family_affluence        VARCHAR(20)     CHECK (family_affluence IN ('AFFLUENT', 'UPPER_MIDDLE', 'MIDDLE', 'LOWER_MIDDLE')),
    family_status           VARCHAR(20)     CHECK (family_status IN ('RICH', 'UPPER_MIDDLE_CLASS', 'MIDDLE_CLASS', 'LOWER_MIDDLE_CLASS')),

    brothers_count          INTEGER         DEFAULT 0,
    brothers_married        INTEGER         DEFAULT 0,
    sisters_count           INTEGER         DEFAULT 0,
    sisters_married         INTEGER         DEFAULT 0,

    -- Vyapar (Business) - specific to Baniya community
    family_business_type    VARCHAR(100),
    business_nature         VARCHAR(100),
    business_turnover_range VARCHAR(50),
    business_locations      VARCHAR(255),

    about_family            TEXT,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_family_details_profile_id ON family_details(profile_id);

-- Education details
CREATE TABLE education_details (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,

    highest_qualification   VARCHAR(50)     NOT NULL
                            CHECK (highest_qualification IN (
                                'BELOW_10TH', '10TH', '12TH', 'DIPLOMA', 'GRADUATE',
                                'POST_GRADUATE', 'DOCTORATE', 'PROFESSIONAL'
                            )),
    qualification_detail    VARCHAR(100),
    institution             VARCHAR(200),
    university              VARCHAR(200),
    passing_year            INTEGER,
    additional_qualification VARCHAR(200),

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_education_details_profile_id ON education_details(profile_id);

-- Profession details
CREATE TABLE profession_details (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,

    employed_in             VARCHAR(30)     CHECK (employed_in IN (
                                'PRIVATE', 'GOVERNMENT', 'BUSINESS', 'SELF_EMPLOYED',
                                'NOT_WORKING', 'STUDENT'
                            )),
    profession              VARCHAR(100),
    employer_name           VARCHAR(200),
    designation             VARCHAR(100),
    annual_income_range     VARCHAR(50),
    work_city               VARCHAR(100),
    work_country            VARCHAR(50)     DEFAULT 'India',

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profession_details_profile_id ON profession_details(profile_id);

-- Lifestyle details
CREATE TABLE lifestyle_details (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,

    diet                    VARCHAR(20)     CHECK (diet IN ('VEGETARIAN', 'JAIN', 'EGGETARIAN', 'NON_VEGETARIAN')),
    smoking                 VARCHAR(20)     CHECK (smoking IN ('NO', 'OCCASIONALLY', 'YES')),
    drinking                VARCHAR(20)     CHECK (drinking IN ('NO', 'OCCASIONALLY', 'YES')),
    hobbies                 TEXT,
    interests               TEXT,
    languages_spoken        VARCHAR(255),

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lifestyle_details_profile_id ON lifestyle_details(profile_id);

-- Profile photos
CREATE TABLE profile_photos (
    id                      BIGSERIAL       PRIMARY KEY,
    profile_id              BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,

    original_key            VARCHAR(500)    NOT NULL,
    medium_key              VARCHAR(500),
    thumbnail_key           VARCHAR(500),
    display_order           INTEGER         NOT NULL DEFAULT 0,
    is_primary              BOOLEAN         NOT NULL DEFAULT FALSE,
    visibility              VARCHAR(20)     NOT NULL DEFAULT 'PUBLIC'
                            CHECK (visibility IN ('PUBLIC', 'CONNECTIONS_ONLY', 'HIDDEN')),
    is_verified             BOOLEAN         NOT NULL DEFAULT FALSE,

    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profile_photos_profile_id ON profile_photos(profile_id);

-- Ensure max 8 photos per profile
CREATE OR REPLACE FUNCTION check_photo_limit()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM profile_photos WHERE profile_id = NEW.profile_id) >= 8 THEN
        RAISE EXCEPTION 'Maximum 8 photos allowed per profile';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_photo_limit
    BEFORE INSERT ON profile_photos
    FOR EACH ROW EXECUTE FUNCTION check_photo_limit();
