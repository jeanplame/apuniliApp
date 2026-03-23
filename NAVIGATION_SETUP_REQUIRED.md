# 🔧 Configuration Navigation Requise

## Actions de Navigation à Ajouter dans mobile_navigation.xml

Ajoutez ces actions **AVANT la fermeture du tag navigation** dans le fichier `app/src/main/res/navigation/mobile_navigation.xml`:

### 1. Depuis MemberDashboardFragment

```xml
<!-- MemberDashboardFragment actions -->
<action
    android:id="@+id/action_member_dashboard_to_member_profile_edit"
    app:destination="@id/nav_member_profile_edit"
    app:enterAnim="@android:anim/slide_in_left"
    app:exitAnim="@android:anim/slide_out_right"
    app:popEnterAnim="@android:anim/slide_in_left"
    app:popExitAnim="@android:anim/slide_out_right" />

<action
    android:id="@+id/action_member_dashboard_to_member_history_detail"
    app:destination="@id/nav_member_history_detail"
    app:enterAnim="@android:anim/slide_in_left"
    app:exitAnim="@android:anim/slide_out_right"
    app:popEnterAnim="@android:anim/slide_in_left"
    app:popExitAnim="@android:anim/slide_out_right" />

<action
    android:id="@+id/action_member_dashboard_to_member_settings"
    app:destination="@id/nav_member_settings"
    app:enterAnim="@android:anim/slide_in_left"
    app:exitAnim="@android:anim/slide_out_right"
    app:popEnterAnim="@android:anim/slide_in_left"
    app:popExitAnim="@android:anim/slide_out_right" />
```

### 2. Fragments à Ajouter

Ajoutez aussi ces **3 fragments** dans le fichier `mobile_navigation.xml`:

```xml
<!-- Member Settings Fragment -->
<fragment
    android:id="@+id/nav_member_settings"
    android:name="com.example.apuniliapp.ui.membership.MemberSettingsFragment"
    android:label="@string/settings"
    tools:layout="@layout/fragment_member_settings" />

<!-- Member Profile Edit Fragment -->
<fragment
    android:id="@+id/nav_member_profile_edit"
    android:name="com.example.apuniliapp.ui.membership.MemberProfileEditFragment"
    android:label="Éditer le Profil"
    tools:layout="@layout/fragment_member_profile_edit" />

<!-- Member History Detail Fragment -->
<fragment
    android:id="@+id/nav_member_history_detail"
    android:name="com.example.apuniliapp.ui.membership.MemberHistoryDetailFragment"
    android:label="@string/membership_history"
    tools:layout="@layout/fragment_member_history_detail" />
```

### 3. Action depuis MembershipFragment vers MemberDashboard

Assurez-vous que cette action existe (elle devrait déjà être là):

```xml
<action
    android:id="@+id/action_membership_to_member_dashboard"
    app:destination="@id/nav_member_dashboard"
    app:enterAnim="@android:anim/fade_in"
    app:exitAnim="@android:anim/fade_out" />
```

---

## Étapes pour Corriger

### Étape 1: Ouvrir le fichier de navigation
```
Fichier: app/src/main/res/navigation/mobile_navigation.xml
```

### Étape 2: Localiser le tag </navigation>
Cherchez la ligne avec `</navigation>` à la fin du fichier

### Étape 3: Ajouter les code
Insérez tout le code ci-dessus AVANT le tag `</navigation>` de fermeture

### Étape 4: Vérifier les imports
Assurez-vous que `tools` est importé dans le header:
```xml
xmlns:tools="http://schemas.android.com/tools"
```

### Étape 5: Compiler et Tester

```bash
# Build
./gradlew build

# Run
Taper F5 dans Android Studio ou Run → Run 'app'
```

---

## Points d'Erreurs Courants

❌ **Erreur 1:** "destination not found"
✅ **Solution:** Assurez-vous que les IDs des fragments existent (nav_member_settings, etc.)

❌ **Erreur 2:** "Cannot find property"
✅ **Solution:** Vérifiez que les noms des actions sont exactement corrects

❌ **Erreur 3:** "Fragment not found"
✅ **Solution:** Vérifiez les chemins complets du package

---

## Fichiers de String à Vérifier

Assurez-vous que ces strings existent dans `strings.xml`:

```xml
<string name="settings">Paramètres</string>
<string name="membership_history">Historique d'Adhésion</string>
```

---

## Structure Finale Attendue

```
mobile_navigation.xml
├─ nav_membership (existant)
│  └─ action → nav_member_dashboard
│
├─ nav_member_dashboard (existant)
│  ├─ action → nav_member_profile_edit
│  ├─ action → nav_member_history_detail
│  ├─ action → nav_member_settings
│  ├─ action → nav_gallery
│  ├─ action → nav_documents
│  └─ action → nav_events
│
├─ nav_member_profile_edit (À AJOUTER)
│
├─ nav_member_history_detail (À AJOUTER)
│
└─ nav_member_settings (À AJOUTER)
```

---

**Date:** 16 Mars 2026
**Status:** Configuration requise AVANT de compiler

