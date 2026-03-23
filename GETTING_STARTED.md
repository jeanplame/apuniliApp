# 🚀 Guide de Démarrage - Application Apunili

## 📋 Prérequis

- Android Studio 2023.1 ou plus récent
- Android SDK 24+ (API 24)
- Kotlin 1.8+
- JDK 11+

## 🔧 Installation

1. **Cloner le projet**
```bash
git clone https://github.com/your-org/apunili-app.git
cd apuniliApp
```

2. **Ouvrir dans Android Studio**
- File → Open → Sélectionner le dossier apuniliApp

3. **Construire le projet**
```bash
./gradlew build
```

4. **Lancer l'application**
```bash
./gradlew installDebug
```

## 👤 Comptes de Test

### Admin
- **Email:** admin@apunili.com
- **Mot de passe:** admin123
- **Rôle:** Administrateur
- **Accès:** Dashboard Admin, Gestion adhésions

### Membre
- **Email:** membre@test.com
- **Mot de passe:** admin123
- **Rôle:** Membre
- **Accès:** Accueil, Activités, Événements, etc.

## 📱 Écrans Principaux

### Pour Tous les Utilisateurs
- **Accueil** - Page d'arrivée avec présentation
- **À propos** - Informations sur l'association
- **Structure** - Équipe et organisation
- **Activités** - Liste des activités
- **Événements** - Calendrier d'événements
- **Galerie** - Photos et vidéos
- **Documents** - Téléchargement documents officiels
- **Adhésion** - Formulaire d'adhésion
- **Contact** - Formulaire de contact
- **Paramètres** - Préférences utilisateur

### Pour les Administrateurs
- **Dashboard Admin** - Vue d'ensemble
- **Gestion Adhésions** - Approuver/Refuser demandes

## 🏗️ Architecture

### Structure du Projet
```
app/
├── src/main/
│   ├── java/com/example/apuniliapp/
│   │   ├── config/          # Configuration centralisée
│   │   ├── constants/       # Constantes globales
│   │   ├── data/
│   │   │   ├── model/       # Modèles de données
│   │   │   └── repository/  # Repositories
│   │   ├── ui/
│   │   │   ├── adapter/     # RecyclerView Adapters
│   │   │   ├── about/
│   │   │   ├── activities/
│   │   │   ├── admin/
│   │   │   ├── auth/
│   │   │   ├── contact/
│   │   │   ├── documents/
│   │   │   ├── events/
│   │   │   ├── gallery/
│   │   │   ├── home/
│   │   │   ├── membership/
│   │   │   ├── settings/
│   │   │   └── structure/
│   │   ├── utils/           # Services utilitaires
│   │   ├── viewmodel/       # ViewModels
│   │   └── MainActivity.kt
│   └── res/
│       ├── layout/          # Layouts
│       ├── menu/            # Menus
│       ├── drawable/        # Images
│       ├── values/          # Ressources
│       └── navigation/      # Navigation graph
└── build.gradle.kts
```

## 🎯 Pattern MVVM

L'application suit le pattern MVVM:
- **View** (Fragment/Activity) - Interface utilisateur
- **ViewModel** - Logique métier et gestion état
- **Model** (Repository) - Accès aux données

## 🔐 Authentification

### Flux de Connexion
1. Utilisateur entre email/mot de passe
2. Service d'authentification valide les données
3. SessionManager sauvegarde la session
4. Redirection vers HomeFragment ou AdminDashboard

### Session Persistante
- SharedPreferences stocke les infos utilisateur
- Session dure 24 heures
- Déconnexion nettoie les données

## 📊 Gestion des Données

Toutes les données sont pré-chargées dans les repositories:

### Activities (4 items)
- Formation informatique
- Atelier sensibilisation
- Distribution alimentaire
- Alphabétisation

### Events (4 items)
- Gala de charité
- Assemblée générale
- Conférence développement
- Journée de bénévolat

### Documents (5 items)
- Statuts
- Règlement Intérieur
- Rapport annuel
- Plan stratégique
- Organigramme

### Team (6 membres)
- Président
- Vice-président
- Secrétaire
- Trésorier
- Coordinateur
- RH

### Gallery (6 items)
- Photos d'activités
- Vidéos d'événements

### Memberships (3 demandes)
- En attente d'approbation
- Avec infos complètes

## 🛠️ Services Disponibles

### SessionManager
Gère la persistance de session utilisateur
```kotlin
val sessionManager = SessionManager(context)
sessionManager.saveSession(user)
sessionManager.getLoggedInUser()
sessionManager.isLoggedIn()
```

### AuthenticationService
Valide et authentifie les utilisateurs
```kotlin
val authService = AuthenticationService(context)
val result = authService.login(email, password)
```

### ValidationService
Valide les entrées utilisateur
```kotlin
ValidationService.isValidEmail(email)
ValidationService.isValidPassword(password)
ValidationService.validateMembershipForm(...)
```

### AppLogger
Logs centralisé
```kotlin
AppLogger.d("Message debug")
AppLogger.logEvent("EventName", mapOf("key" to "value"))
```

## 🎨 Thème et Ressources

- **Couleur primaire:** Material Design 3
- **Langue:** Français
- **Support multi-écran:** Oui (phone, tablet)
- **Dark Mode:** Prêt pour implémentation

## 📝 Conventions de Code

### Nommage
- Classes: PascalCase (MainActivity)
- Fonctions: camelCase (loadData())
- Constantes: UPPER_SNAKE_CASE (MAX_SIZE)
- Variables privées: _name

### Imports
```kotlin
// Organiser par: android, androidx, com.google, com.example
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.example.apuniliapp.data.model.User
```

## 🐛 Dépannage

### Application ne compile pas
- Vérifier que Android SDK 24+ est installé
- Faire File → Sync Now
- Nettoyer: ./gradlew clean build

### SharedPreferences non persistées
- Vérifier les permissions dans AndroidManifest.xml
- SessionManager initialisation correcte

### RecyclerView vide
- Vérifier que ListAdapter.submitList() est appelé
- Vérifier les ViewHolder bindings

## 📚 Ressources Utiles

- [Google Architecture Samples](https://github.com/android/architecture-samples)
- [Android Developers Documentation](https://developer.android.com)
- [Kotlin Official](https://kotlinlang.org)
- [Material Design 3](https://m3.material.io)

## 📞 Support

Pour des questions ou problèmes:
- Ouvrir une issue sur GitHub
- Contacter: support@apunili.com
- Documentation: /docs

## 📄 License

Ce projet est sous license MIT.

---

**Créé:** 2026-03-08
**Version:** 1.0.0
**Statut:** Production-Ready ✅

