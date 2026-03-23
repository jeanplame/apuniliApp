# 🚀 Guide de configuration Supabase — Apunili ASBL

## Étapes à suivre dans le Dashboard Supabase

Ouvrez votre projet : **https://supabase.com/dashboard** → Projet `vatydmnvdxhllofoxszl`

---

### 1️⃣ Activer l'authentification Email/Password

1. **Authentication** → **Providers** (menu à gauche)
2. Cherchez **Email** et activez-le
3. Options recommandées :
   - ✅ Enable Email Signup
   - ❌ Désactiver "Confirm email" (pour le développement)
   - Cliquez **Save**

---

### 2️⃣ Créer les tables et le bucket Storage

1. **SQL Editor** (menu à gauche) → **New Query**
2. Copiez-collez le contenu intégral du fichier `supabase_setup.sql` (à la racine du projet)
3. Cliquez **Run**
4. Vous devriez voir un message de succès

Ce script crée :
- ✅ 9 tables : `users`, `activities`, `events`, `documents`, `gallery`, `membership_requests`, `organization`, `team_members`, `audit_logs`
- ✅ Politiques RLS (Row Level Security) pour chaque table
- ✅ Bucket Storage `media` (public)
- ✅ Politiques Storage pour upload/download

---

### 3️⃣ Vérifier la configuration

Après avoir exécuté le script SQL :

1. **Table Editor** → Vérifiez que les 9 tables apparaissent
2. **Storage** → Vérifiez que le bucket `media` est créé et public
3. **Authentication** → **Providers** → Vérifiez que Email est activé

---

### 4️⃣ Lancer l'application

1. Lancez l'app sur l'émulateur ou appareil
2. Le **SupabaseSeeder** va automatiquement :
   - Créer 2 comptes de test (admin + membre)
   - Insérer les données de démonstration (activités, événements, documents, etc.)
3. Connectez-vous avec :
   - 📧 **admin@apunili.com** / 🔑 **admin123**
   - 📧 **membre@test.com** / 🔑 **admin123**

---

### 5️⃣ Diagnostic intégré

L'app inclut un outil de diagnostic Supabase (`FirebaseDiagnostic.kt`) qui teste :
- ✅ Auth Email/Password
- ✅ Postgrest (lecture des tables)
- ✅ Storage (upload dans le bucket `media`)

Si un service échoue, le dialogue vous indiquera exactement quoi corriger.

---

## Résumé de la migration Firebase → Supabase

| Composant | Avant (Firebase) | Après (Supabase) |
|-----------|------------------|-------------------|
| Auth | `FirebaseAuth` | `supabase.auth` (Email) |
| Base de données | Cloud Firestore | Postgrest (PostgreSQL) |
| Stockage | Firebase Storage | Supabase Storage |
| Config | `google-services.json` | `SupabaseClient.kt` |
| Modèles | Maps Firestore | `@Serializable` + `@SerialName` |

### Fichiers modifiés
- 12 modèles de données (annotations `@Serializable`)
- 9 repositories (réécrits pour Postgrest)
- Auth : `SessionManager`, `AuthenticationService`, `LoginFragment`, `RegisterFragment`
- Storage : `AdminPublishFragment`
- Utils : `SupabaseSeeder` (nouveau), `FirebaseDiagnostic` (réécrit)
- `MainActivity` (Seeder + Auth)
- `google-services.json` (supprimé)

