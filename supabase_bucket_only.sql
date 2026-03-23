-- ═══════════════════════════════════════════════════════════════
-- TOUT-EN-UN : Tables + Bucket Storage + Policies
-- Copier-coller TOUT dans : SQL Editor → New Query → Run
-- Projet: vatydmnvdxhllofoxszl
-- ═══════════════════════════════════════════════════════════════

-- 1. BUCKET STORAGE "media"
INSERT INTO storage.buckets (id, name, public)
VALUES ('media', 'media', true)
ON CONFLICT (id) DO NOTHING;

-- 2. POLICIES STORAGE
DO $$ BEGIN
  CREATE POLICY "media_select" ON storage.objects FOR SELECT USING (bucket_id = 'media');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  CREATE POLICY "media_insert" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'media');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  CREATE POLICY "media_update" ON storage.objects FOR UPDATE USING (bucket_id = 'media');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  CREATE POLICY "media_delete" ON storage.objects FOR DELETE USING (bucket_id = 'media');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ✅ TERMINÉ — Le bucket "media" est créé et accessible.

