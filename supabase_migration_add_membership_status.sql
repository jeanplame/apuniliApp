-- ═══════════════════════════════════════════════════════════════════════
-- MIGRATION: Ajouter le champ membership_status à la table users
-- ═══════════════════════════════════════════════════════════════════════
-- À exécuter si la table users existe déjà

-- 1. Ajouter la colonne membership_status si elle n'existe pas
ALTER TABLE users
ADD COLUMN IF NOT EXISTS membership_status TEXT NOT NULL DEFAULT 'NOT_SUBMITTED';

-- 2. Changer le rôle par défaut de VISITOR à MEMBER pour les nouvelles inscriptions
ALTER TABLE users
ALTER COLUMN role SET DEFAULT 'MEMBER';

-- Afficher le résultat
SELECT * FROM users LIMIT 1;

