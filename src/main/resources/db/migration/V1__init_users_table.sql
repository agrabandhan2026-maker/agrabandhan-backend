-- ============================================
-- V1__init_users_table.sql
-- AgraBandhan - Initial schema
-- ============================================

-- Create custom types
CREATE TYPE user_role AS ENUM ('USER', 'PREMIUM_USER', 'MODERATOR', 'ADMIN');
CREATE TYPE gotra_type AS ENUM (
    'AIRAN', 'BANSAL', 'BHANDAL', 'BINDAL', 'DHARAN',
    'GARG', 'GOYAL', 'GOYAN', 'JINDAL', 'KANSAL',
    'KUCHHAL', 'MADHUKUL', 'MANGAL', 'MITTAL', 'NAGIL',
    'SINGHAL', 'TAYAL', 'TINGAL'
);

-- Users table (auth)
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    phone_number    VARCHAR(15)     NOT NULL UNIQUE,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    is_profile_complete BOOLEAN     NOT NULL DEFAULT FALSE,
    fcm_token       TEXT,
    refresh_token   TEXT,
    last_login_at   TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
