-- Exécuter dans : Supabase Dashboard → SQL Editor → New Query
-- Crée le bucket "media" et ses policies d'accès

-- 1. Créer le bucket
INSERT INTO storage.buckets (id, name, public)
VALUES ('media', 'media', true)
ON CONFLICT (id) DO NOTHING;

-- 2. Policy : tout le monde peut LIRE les fichiers
CREATE POLICY "media_public_read" ON storage.objects
    FOR SELECT TO public
    USING (bucket_id = 'media');

-- 3. Policy : tout le monde peut UPLOADER des fichiers
CREATE POLICY "media_public_insert" ON storage.objects
    FOR INSERT TO public
    WITH CHECK (bucket_id = 'media');

-- 4. Policy : tout le monde peut MODIFIER des fichiers
CREATE POLICY "media_public_update" ON storage.objects
    FOR UPDATE TO public
    USING (bucket_id = 'media');

-- 5. Policy : tout le monde peut SUPPRIMER des fichiers
CREATE POLICY "media_public_delete" ON storage.objects
    FOR DELETE TO public
    USING (bucket_id = 'media');
