-- ═══════════════════════════════════════════════════════════════════════
-- SCRIPT SQL COMPLET — Supabase Setup pour Apunili ASBL
-- À exécuter dans : Supabase Dashboard → SQL Editor → New Query
-- ═══════════════════════════════════════════════════════════════════════
--
-- ⚠️ IMPORTANT — AVANT d'exécuter ce script :
--   1. Authentication → Providers → Email → ACTIVER
--   2. Authentication → Settings → cocher "Allow new users to sign up"
--   3. Authentication → Email Templates → Confirm signup :
--      DÉCOCHER "Enable email confirmations" (pour le développement)
--      OU : Authentication → Settings → "Confirm email" → OFF
--
-- ═══════════════════════════════════════════════════════════════════════

-- ════════════════════════════════════════════════════
-- 1. TABLE: users
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS users (
    id                  TEXT PRIMARY KEY,
    email               TEXT NOT NULL DEFAULT '',
    display_name        TEXT NOT NULL DEFAULT '',
    photo_url           TEXT NOT NULL DEFAULT '',
    password_hash       TEXT NOT NULL DEFAULT '',
    role                TEXT NOT NULL DEFAULT 'MEMBER',
    membership_status   TEXT NOT NULL DEFAULT 'NOT_SUBMITTED',
    created_at          BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Tout le monde peut lire les profils
CREATE POLICY "users_select_all" ON users
    FOR SELECT USING (true);

-- Les utilisateurs connectés peuvent insérer/mettre à jour leur propre profil
CREATE POLICY "users_insert_own" ON users
    FOR INSERT WITH CHECK (true);

CREATE POLICY "users_update_own" ON users
    FOR UPDATE USING (true);

-- ════════════════════════════════════════════════════
-- 2. TABLE: activities
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS activities (
    id          TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    title       TEXT NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    date        TEXT NOT NULL DEFAULT '',
    location    TEXT NOT NULL DEFAULT '',
    category    TEXT NOT NULL DEFAULT '',
    photo_urls  JSONB NOT NULL DEFAULT '[]'::JSONB,
    created_at  BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE activities ENABLE ROW LEVEL SECURITY;

CREATE POLICY "activities_select_all" ON activities
    FOR SELECT USING (true);

CREATE POLICY "activities_insert_auth" ON activities
    FOR INSERT WITH CHECK (true);

CREATE POLICY "activities_update_auth" ON activities
    FOR UPDATE USING (true);

CREATE POLICY "activities_delete_auth" ON activities
    FOR DELETE USING (true);

-- ════════════════════════════════════════════════════
-- 3. TABLE: events
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS events (
    id          TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    title       TEXT NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    date        TEXT NOT NULL DEFAULT '',
    location    TEXT NOT NULL DEFAULT '',
    photo_urls  JSONB NOT NULL DEFAULT '[]'::JSONB,
    video_urls  JSONB NOT NULL DEFAULT '[]'::JSONB,
    created_at  BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "events_select_all" ON events
    FOR SELECT USING (true);

CREATE POLICY "events_insert_auth" ON events
    FOR INSERT WITH CHECK (true);

CREATE POLICY "events_update_auth" ON events
    FOR UPDATE USING (true);

CREATE POLICY "events_delete_auth" ON events
    FOR DELETE USING (true);

-- ════════════════════════════════════════════════════
-- 4. TABLE: documents
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS documents (
    id          TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    title       TEXT NOT NULL DEFAULT '',
    type        TEXT NOT NULL DEFAULT '',
    category    TEXT NOT NULL DEFAULT 'OTHER',
    description TEXT NOT NULL DEFAULT '',
    file_url    TEXT NOT NULL DEFAULT '',
    file_size   TEXT NOT NULL DEFAULT '',
    created_at  BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE documents ENABLE ROW LEVEL SECURITY;

CREATE POLICY "documents_select_all" ON documents
    FOR SELECT USING (true);

CREATE POLICY "documents_insert_auth" ON documents
    FOR INSERT WITH CHECK (true);

CREATE POLICY "documents_update_auth" ON documents
    FOR UPDATE USING (true);

CREATE POLICY "documents_delete_auth" ON documents
    FOR DELETE USING (true);

-- ════════════════════════════════════════════════════
-- 5. TABLE: gallery
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS gallery (
    id          TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    title       TEXT NOT NULL DEFAULT '',
    description TEXT NOT NULL DEFAULT '',
    image_url   TEXT NOT NULL DEFAULT '',
    video_url   TEXT NOT NULL DEFAULT '',
    type        TEXT NOT NULL DEFAULT 'image',
    category    TEXT NOT NULL DEFAULT '',
    album_id    TEXT NOT NULL DEFAULT '',
    created_at  BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE gallery ENABLE ROW LEVEL SECURITY;

CREATE POLICY "gallery_select_all" ON gallery
    FOR SELECT USING (true);

CREATE POLICY "gallery_insert_auth" ON gallery
    FOR INSERT WITH CHECK (true);

CREATE POLICY "gallery_update_auth" ON gallery
    FOR UPDATE USING (true);

CREATE POLICY "gallery_delete_auth" ON gallery
    FOR DELETE USING (true);

-- ════════════════════════════════════════════════════
-- 6. TABLE: membership_requests
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS membership_requests (
    id                TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    lastname          TEXT NOT NULL DEFAULT '',
    postname          TEXT NOT NULL DEFAULT '',
    firstname         TEXT NOT NULL DEFAULT '',
    gender            TEXT NOT NULL DEFAULT '',
    birthdate         TEXT NOT NULL DEFAULT '',
    address           TEXT NOT NULL DEFAULT '',
    phone             TEXT NOT NULL DEFAULT '',
    email             TEXT NOT NULL DEFAULT '',
    profession        TEXT NOT NULL DEFAULT '',
    photo_url         TEXT NOT NULL DEFAULT '',
    id_card_url       TEXT NOT NULL DEFAULT '',
    accepted_statutes BOOLEAN NOT NULL DEFAULT FALSE,
    accepted_roi      BOOLEAN NOT NULL DEFAULT FALSE,
    accepted_values   BOOLEAN NOT NULL DEFAULT FALSE,
    status            TEXT NOT NULL DEFAULT 'PENDING',
    admin_comment     TEXT NOT NULL DEFAULT '',
    reviewed_at       BIGINT NOT NULL DEFAULT 0,
    reviewed_by       TEXT NOT NULL DEFAULT '',
    created_at        BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE membership_requests ENABLE ROW LEVEL SECURITY;

CREATE POLICY "membership_select_all" ON membership_requests
    FOR SELECT USING (true);

CREATE POLICY "membership_insert_auth" ON membership_requests
    FOR INSERT WITH CHECK (true);

CREATE POLICY "membership_update_auth" ON membership_requests
    FOR UPDATE USING (true);

CREATE POLICY "membership_delete_auth" ON membership_requests
    FOR DELETE USING (true);

-- ════════════════════════════════════════════════════
-- 7. TABLE: organization
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS organization (
    id            TEXT PRIMARY KEY DEFAULT '1',
    name          TEXT NOT NULL DEFAULT '',
    mission       TEXT NOT NULL DEFAULT '',
    vision        TEXT NOT NULL DEFAULT '',
    objectives    TEXT NOT NULL DEFAULT '',
    address       TEXT NOT NULL DEFAULT '',
    phone         TEXT NOT NULL DEFAULT '',
    email         TEXT NOT NULL DEFAULT '',
    website       TEXT NOT NULL DEFAULT '',
    history       TEXT NOT NULL DEFAULT '',
    logo_url      TEXT NOT NULL DEFAULT '',
    founded_date  TEXT NOT NULL DEFAULT '',
    last_modified BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE organization ENABLE ROW LEVEL SECURITY;

CREATE POLICY "organization_select_all" ON organization
    FOR SELECT USING (true);

CREATE POLICY "organization_insert_auth" ON organization
    FOR INSERT WITH CHECK (true);

CREATE POLICY "organization_update_auth" ON organization
    FOR UPDATE USING (true);

-- ════════════════════════════════════════════════════
-- 8. TABLE: team_members
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS team_members (
    id         TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    firstname  TEXT NOT NULL DEFAULT '',
    lastname   TEXT NOT NULL DEFAULT '',
    position   TEXT NOT NULL DEFAULT '',
    email      TEXT NOT NULL DEFAULT '',
    phone      TEXT NOT NULL DEFAULT '',
    photo_url  TEXT NOT NULL DEFAULT '',
    bio        TEXT NOT NULL DEFAULT '',
    department TEXT NOT NULL DEFAULT '',
    created_at BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE team_members ENABLE ROW LEVEL SECURITY;

CREATE POLICY "team_members_select_all" ON team_members
    FOR SELECT USING (true);

CREATE POLICY "team_members_insert_auth" ON team_members
    FOR INSERT WITH CHECK (true);

CREATE POLICY "team_members_update_auth" ON team_members
    FOR UPDATE USING (true);

CREATE POLICY "team_members_delete_auth" ON team_members
    FOR DELETE USING (true);

-- ════════════════════════════════════════════════════
-- 9. TABLE: audit_logs
-- ════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS audit_logs (
    id        TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    user_id   TEXT NOT NULL DEFAULT '',
    user_name TEXT NOT NULL DEFAULT '',
    action    TEXT NOT NULL DEFAULT 'LOGIN',
    details   TEXT NOT NULL DEFAULT '',
    "timestamp" BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "audit_logs_select_all" ON audit_logs
    FOR SELECT USING (true);

CREATE POLICY "audit_logs_insert_auth" ON audit_logs
    FOR INSERT WITH CHECK (true);

-- ═══════════════════════════════════════════════════════════════════════
-- ✅ TABLES CRÉÉES !
--
-- Il reste 2 choses à faire MANUELLEMENT dans le Dashboard :
--
-- A) STORAGE — Créer le bucket "media" :
--    1. Dashboard → Storage → New Bucket
--    2. Nom : media
--    3. Cocher "Public bucket"
--    4. Cliquer Create
--    5. Aller dans Policies du bucket → New Policy → "For full customization"
--    6. Policy name: allow_all, Allowed operation: ALL, Policy: true → Save
--
-- B) AUTH — Vérifier la config :
--    1. Dashboard → Authentication → Providers → Email → Activé
--    2. Dashboard → Authentication → Settings → "Confirm email" → DÉSACTIVÉ
--
-- Ensuite lancez l'app → le Seeder crée les données de test.
-- Connectez-vous avec : admin@apunili.com / admin123
-- ═══════════════════════════════════════════════════════════════════════

