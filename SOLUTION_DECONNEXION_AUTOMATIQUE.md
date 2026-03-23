# 🔧 Solution: Correction de la Déconnexion Automatique

## ❌ PROBLÈME IDENTIFIÉ

Vous étiez automatiquement déconnecté après quelques minutes (environ 1 heure) de la session Supabase.

### Cause racine :
Supabase Auth, par défaut, expire les sessions après **3600 secondes (1 heure)**. Votre code vérifiait uniquement `supabaseAuth.currentUserOrNull()` pour déterminer si l'utilisateur est connecté. Quand le token expirait:

1. `supabaseAuth.currentUserOrNull()` retournait `null`
2. La méthode `isLoggedIn()` nettoyait la session locale
3. L'utilisateur était automatiquement déconnecté

**Aucun mécanisme de renouvellement de token n'existait.**

---

## ✅ SOLUTION IMPLÉMENTÉE

### 1. **Amélioration de SessionManager.kt**

#### Nouvelle gestion des tokens:
- Ajout de sauvegarde du timestamp d'expiration (`KEY_TOKEN_EXPIRES_AT`)
- Amélioration de `isLoggedIn()` pour maintenir la session même si Supabase Auth a expiré
- Nouvelle méthode `refreshSessionIfNeeded()` pour renouveler automatiquement

```kotlin
/**
 * Renouvelle la session Supabase si elle est proche de l'expiration.
 * Doit être appelé régulièrement.
 */
suspend fun refreshSessionIfNeeded(): Boolean {
    // Renouveler si expiration dans les 5 minutes
    if (timeUntilExpiry < (5 * 60 * 1000)) {
        supabaseAuth.refreshCurrentSession()
        // Mettre à jour l'heure d'expiration
    }
    return true
}
```

#### Nouvelle logique isLoggedIn():
```kotlin
fun isLoggedIn(): Boolean {
    val localLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    if (!localLoggedIn) return false
    
    // Si Supabase a une session active, c'est bon
    if (supabaseAuth.currentUserOrNull() != null) return true
    
    // Si Supabase n'a pas de session mais la session locale n'est pas expirée
    val tokenExpiresAt = sharedPreferences.getLong(KEY_TOKEN_EXPIRES_AT, 0)
    if (tokenExpiresAt > System.currentTimeMillis()) return true
    
    // Session expirée, nettoyer
    clearLocalSession()
    return false
}
```

---

### 2. **Nouveau Service: SessionRefreshWorker.kt**

Crée un travail périodique qui renouvelle automatiquement la session toutes les **15 minutes** en arrière-plan:

```kotlin
class SessionRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)
        
        if (!sessionManager.isLoggedIn()) return Result.success()
        
        val refreshed = sessionManager.refreshSessionIfNeeded()
        return if (refreshed) Result.success() else Result.retry()
    }
}
```

**Avantages:**
- ✅ Renouvelle automatiquement le token avant expiration
- ✅ Fonctionne même si l'app est en arrière-plan
- ✅ S'arrête intelligemment lors de la déconnexion

---

### 3. **Intégration dans AuthenticationService.kt**

- **À la connexion:** Démarre le travail de renouvellement
  ```kotlin
  SessionRefreshWorker.startPeriodicRefresh(context)
  ```

- **À la déconnexion:** Arrête le travail
  ```kotlin
  SessionRefreshWorker.stopPeriodicRefresh(context)
  ```

---

### 4. **Intégration dans MainActivity.kt**

- Démarre le renouvellement à l'initialisation si l'utilisateur est déjà connecté
- Rafraîchit la session à chaque retour au premier plan (onResume)

```kotlin
if (sessionManager.isLoggedIn()) {
    SessionRefreshWorker.startPeriodicRefresh(this)
    Log.d("MainActivity", "Renouvellement de session activé")
}
```

---

### 5. **Ajout de la dépendance WorkManager**

**Fichier:** `gradle/libs.versions.toml`
```toml
workManager = "2.8.1"
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
```

**Fichier:** `app/build.gradle.kts`
```gradle
implementation(libs.androidx.work.runtime.ktx)
```

---

## 📊 Flux de Session Amélioré

```
┌─────────────────────────────────────────┐
│   Utilisateur se connecte              │
└────────────────┬────────────────────────┘
                 │
                 ▼
    ┌─────────────────────────────┐
    │ AuthenticationService.login │
    │  - Authentification         │
    │  - Sauvegarde session       │
    │  - Sauvegarde expiration    │
    │  - Lance renouvellement     │
    └────────────┬────────────────┘
                 │
                 ▼
    ┌──────────────────────────────┐
    │ SessionRefreshWorker         │
    │ (Toutes les 15 minutes)      │
    │                              │
    │ if (expiration < 5 min)      │
    │   → refreshCurrentSession()  │
    │   → Sauvegarde nouvelle      │
    │     expiration               │
    └────────────┬─────────────────┘
                 │
                 ▼
    ┌──────────────────────────────┐
    │ Session Maintenue Vivante    │
    │ L'utilisateur reste connecté │
    └──────────────────────────────┘
```

---

## 🧪 Résultats Attendus

### Avant la correction:
- ❌ Déconnexion après ~1 heure
- ❌ Token expiration non gérée
- ❌ Pas de mécanisme de renouvellement

### Après la correction:
- ✅ Session maintenue indéfiniment (tant que l'app est utilisée)
- ✅ Renouvellement transparent toutes les 15 minutes
- ✅ Pas de déconnexion surprise
- ✅ Fonctionne en arrière-plan
- ✅ S'arrête quand l'utilisateur se déconnecte

---

## 📝 Notes Techniques

### Points clés:
1. **WorkManager** gère le renouvellement en arrière-plan de façon fiable
2. **Renouvellement à 5 minutes avant expiration** donne du temps d'avance
3. **Vérification toutes les 15 minutes** = bonne balance entre batterie et fraîcheur
4. **Sauvegarde persistante** des données de session en SharedPreferences

### Configuration Supabase:
- Token d'accès: 3600 secondes (1 heure)
- Token de rafraîchissement: Plus long (généralement 7 jours)
- Renouvellement géré par `refreshCurrentSession()`

---

## 🔍 Déboguer

Cherchez dans les logs:
```
D/SessionManager: Session renouvelée avec succès
D/SessionRefreshWorker: Session rafraîchie avec succès
W/SessionRefreshWorker: Échec du rafraîchissement, réessai
```

---

## ✨ Résumé des Changements

| Fichier | Action |
|---------|--------|
| `SessionManager.kt` | ✅ Amélioré pour gérer tokens et expirations |
| `SessionRefreshService.kt` | ✅ Créé (renouvellement automatique) |
| `AuthenticationService.kt` | ✅ Intégration du renouvellement |
| `MainActivity.kt` | ✅ Lancement du renouvellement |
| `gradle/libs.versions.toml` | ✅ Ajout WorkManager |
| `app/build.gradle.kts` | ✅ Ajout dépendance WorkManager |

---

## 🚀 Déploiement

1. Compilez le projet: `./gradlew clean build`
2. Testez la connexion et attendez 15 minutes
3. Vérifiez les logs pour confirmer le renouvellement
4. Vous ne devriez plus être déconnecté automatiquement

---

## 📞 Support

Si la session expire encore:
1. Vérifiez les logs WorkManager
2. Assurez-vous que WorkManager n'est pas désactivé par les paramètres batterie
3. Vérifiez la configuration Supabase pour la longueur du token
4. Augmentez la fréquence de renouvellement si nécessaire

