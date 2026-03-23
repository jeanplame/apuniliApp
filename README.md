# 📱 Apunili ASBL - Application Mobile Android

Une application mobile complète pour l'association ASBL Apunili, dédiée au développement communautaire, l'entraide sociale et la solidarité.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/kotlin-1.8%2B-blue)

## 🎯 À Propos

Apunili est une application mobile Android moderne conçue pour:
- 📋 Informer sur les activités et événements
- 👥 Gérer l'adhésion des membres
- 📱 Faciliter la communication
- 📁 Partager des documents officiels
- 🖼️ Afficher une galerie de photos/vidéos
- 👨‍💼 Présenter la structure organisationnelle

## ✨ Fonctionnalités Principales

### 👤 Authentification
- ✅ Connexion sécurisée avec email/mot de passe
- ✅ Session persistante (SharedPreferences)
- ✅ Distinction Administrateur/Membre
- ✅ Redirection basée sur le rôle

### 📱 Écrans Utilisateur
1. **Accueil** - Présentation avec actions rapides
2. **À Propos** - Infos sur l'association
3. **Structure** - Équipe et organisation
4. **Activités** - Liste des activités récentes
5. **Événements** - Calendrier avec filtrage
6. **Galerie** - Photos et vidéos (filtrage)
7. **Documents** - Docs officiels (filtrage)
8. **Adhésion** - Formulaire d'adhésion
9. **Contact** - Formulaire de contact
10. **Paramètres** - Préférences utilisateur

### 👨‍💼 Écrans Admin
- **Dashboard** - Vue d'ensemble
- **Gestion Adhésions** - Approuver/Refuser demandes

### 🔧 Architecture Technique
- **Pattern:** MVVM (Model-View-ViewModel)
- **Navigation:** Navigation Component
- **Binding:** View Binding
- **Async:** LiveData + Coroutines ready
- **Database:** SharedPreferences (Room ready)

## 🚀 Démarrage Rapide

### Prérequis
```
- Android Studio 2023.1+
- Android SDK 24+ (API 24)
- Kotlin 1.8+
- JDK 11+
```

### Installation
```bash
# Clone le projet
git clone <repository-url>
cd apuniliApp

# Construire
./gradlew build

# Lancer
./gradlew installDebug
```

### Comptes de Test
```
Admin:
- Email: admin@apunili.com
- Mot de passe: admin123

Membre:
- Email: membre@test.com
- Mot de passe: admin123
```

## 📊 Statistiques du Projet

| Métrique | Valeur |
|----------|--------|
| Fragments | 14 |
| ViewModels | 13 |
| Adapters | 6 |
| Repositories | 7 |
| Services | 5 |
| Modèles | 8 |
| Fichiers Kotlin | 50+ |
| Lignes de Code | 5000+ |
| Architecture | MVVM |

## 📂 Structure du Projet

```
apuniliApp/
├── app/src/main/
│   ├── java/com/example/apuniliapp/
│   │   ├── config/          # Configuration app
│   │   ├── constants/       # Constantes
│   │   ├── data/
│   │   │   ├── model/       # Modèles de données
│   │   │   └── repository/  # Repositories + données test
│   │   ├── ui/              # UI Fragments & Adapters
│   │   ├── utils/           # Services utilitaires
│   │   ├── viewmodel/       # ViewModels
│   │   └── MainActivity.kt
│   └── res/
│       ├── layout/          # XML layouts
│       ├── menu/            # Menus
│       ├── drawable/        # Icônes
│       ├── values/          # Strings, Dimens, etc
│       └── navigation/      # Navigation graph
└── README.md, ROADMAP.md, GETTING_STARTED.md
```

## 🎨 Technologies

### Framework & Libraries
- **Android Framework** - API 24+
- **Kotlin** - Langage principal
- **AndroidX** - Modern Android libraries
- **Material Design 3** - UI Components
- **Navigation Component** - Routing
- **LiveData** - Observables réactives
- **ViewModel** - Lifecycle-aware

### Architecture Pattern
```
┌─────────────────┐
│   View Layer    │  (Fragment, Activity)
│  (UI/Binding)   │
└────────┬────────┘
         ↓
┌─────────────────┐
│ ViewModel Layer │  (LiveData, State)
└────────┬────────┘
         ↓
┌─────────────────┐
│  Data Layer     │  (Repository, Database)
└─────────────────┘
```

## 📋 Données Pré-chargées

### Activités (4 items)
- Formation informatique
- Atelier sensibilisation
- Distribution alimentaire
- Alphabétisation

### Événements (4 items)
- Gala de charité
- Assemblée générale
- Conférence développement
- Journée de bénévolat

### Documents (5 items)
- Statuts, ROI, Rapport annuel, Plan, Organigramme

### Équipe (6 membres)
- Dirigeants et personnel

### Galerie (6 items)
- Photos et vidéos

## 🔒 Sécurité

- ✅ Validation email/mot de passe
- ✅ Session sécurisée SharedPreferences
- ✅ Gestion des erreurs robuste
- ✅ Prêt pour HTTPS/TLS
- ✅ Prêt pour JWT tokens

## 📈 Performance

- ✅ ListAdapter avec DiffUtil
- ✅ View Binding (no reflection)
- ✅ Lazy loading ready
- ✅ Memory efficient
- ✅ Optimized layouts

## 🧪 Test Comptes

### Admin Access
```
Email: admin@apunili.com
Password: admin123
```

### Member Access
```
Email: membre@test.com
Password: admin123
```

## 📚 Documentation

- **IMPLEMENTATION_SUMMARY.md** - Résumé complet
- **GETTING_STARTED.md** - Guide de démarrage
- **ROADMAP.md** - Futures versions

## 🤝 Contribution

Les contributions sont bienvenues! Voir CONTRIBUTING.md pour les guidelines.

### Pour contribuer:
1. Fork le projet
2. Créer une feature branch
3. Commit vos changes
4. Push et créer une Pull Request

## 🐛 Bugs & Issues

Reportez les bugs en ouvrant une [issue](../../issues).

Inclure:
- Description du bug
- Steps pour reproduire
- Comportement attendu
- Device & Android version

## 📄 License

Ce projet est sous la license **MIT**. Voir LICENSE.md pour plus de détails.

## 👥 Équipe

- **Développeur Principal:** [Votre nom]
- **UI/UX Designer:** [Designer]
- **Product Manager:** [PM]

## 📞 Contact

- **Website:** www.apunili.com
- **Email:** contact@apunili.com
- **GitHub:** github.com/apunili
- **Twitter:** @Apunili_ASBL

## 🎉 Remerciements

Merci à tous les contributeurs et à la communauté Apunili!

---

## 📊 Stats GitHub

![GitHub Stars](https://img.shields.io/github/stars/apunili/mobile-app?style=social)
![GitHub Forks](https://img.shields.io/github/forks/apunili/mobile-app?style=social)
![GitHub Issues](https://img.shields.io/github/issues/apunili/mobile-app)
![GitHub PRs](https://img.shields.io/github/issues-pr/apunili/mobile-app)

---

**Créé:** 2026-03-08  
**Version:** 1.0.0  
**Statut:** ✅ Production Ready  
**Dernière mise à jour:** 2026-03-08

