-- ============================================
-- V3__search_views_shortlist.sql
-- AgraBandhan - Profile views, shortlists,
-- and search optimization indexes
-- ============================================

-- Profile views (who viewed whom)
CREATE TABLE profile_views (
    id                  BIGSERIAL       PRIMARY KEY,
    viewer_profile_id   BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    viewed_profile_id   BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    viewed_at           TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_profile_view UNIQUE (viewer_profile_id, viewed_profile_id)
);

CREATE INDEX idx_profile_views_viewer ON profile_views(viewer_profile_id, viewed_at DESC);
CREATE INDEX idx_profile_views_viewed ON profile_views(viewed_profile_id, viewed_at DESC);

-- Shortlisted profiles
CREATE TABLE shortlisted_profiles (
    id                  BIGSERIAL       PRIMARY KEY,
    profile_id          BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    shortlisted_id      BIGINT          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_shortlist UNIQUE (profile_id, shortlisted_id)
);

CREATE INDEX idx_shortlist_profile ON shortlisted_profiles(profile_id, created_at DESC);

-- Additional search indexes on profiles
CREATE INDEX idx_profiles_height ON profiles(height_cm);
CREATE INDEX idx_profiles_completeness ON profiles(completeness_score DESC);
CREATE INDEX idx_profiles_created ON profiles(created_at DESC);

-- Composite index for common search patterns
CREATE INDEX idx_profiles_gender_gotra_city ON profiles(gender, gotra, current_city);
CREATE INDEX idx_profiles_gender_gotra_state ON profiles(gender, gotra, current_state);

-- Index on education for search
CREATE INDEX idx_education_qualification ON education_details(highest_qualification);

-- Index on profession for search
CREATE INDEX idx_profession_employed ON profession_details(employed_in);
CREATE INDEX idx_profession_income ON profession_details(annual_income_range);

-- Index on lifestyle for search
CREATE INDEX idx_lifestyle_diet ON lifestyle_details(diet);
