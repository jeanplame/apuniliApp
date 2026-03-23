# 📐 Diagrammes & Structure Visuelle - Nouvelles Interfaces

## 1. 📊 Navigation Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│                 MembershipFragment                  │
│            (Route d'entrée: nav_membership)         │
└────────────────────┬────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
    NOT_SUBMITTED PENDING    ACCEPTED
        │            │            │
        │            │            └──────────────────────┐
        │            │                                   │
        │            │          ┌────────────────────────▼──────┐
        │            │          │   MemberDashboardFragment      │
        │            │          │   (Écran principal membre)     │
        │            │          └────────────┬───────────────────┘
        │            │                       │
        │            │          ┌────────────┼────────┬──────────────┬──────────┐
        │            │          │            │        │              │          │
        │            │     View Profile  View History Settings     Gallery  Documents
        │            │          │            │        │              │          │
        │            │          ▼            ▼        ▼              ▼          ▼
        │            │  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐
        │            │  │MemberProfile │  │  MemberHistory│  │ MemberSettingsFragment│
        │            │  │EditFragment  │  │DetailFragment │  │ (nav_member_settings) │
        │            │  └──────────────┘  └──────────────┘  └───────────────────┘
        │            │                           │                    │
        │            │                      [Timeline                 │
        │            │                       Events]           Change Password
        │            │                                         Notifications
        │            │                                         About
        │            │                                         Logout
        │            │                                             │
        │            │                                         Nav Login
        │            │                                             │
        │            │                                             ▼
        │            │                                        nav_login

REJECTED ─────────────────────────────────────────────────────────────────────────▶ Resubmit Form

Legend:
  → Navigation
  │ Flow
  ▼ Direction
```

---

## 2. 🎨 Layout Structure - MemberDashboardFragment

```
┌────────────────────────────────────────┐
│  NestedScrollView (Main Container)     │
├────────────────────────────────────────┤
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  HERO PROFILE CARD               │ │
│  │  ┌────────┐                      │ │
│  │  │[Photo] │ Jean Dupont  [ACTIF]│ │
│  │  │        │ Email               │ │
│  │  │        │ Membre depuis 15 mar│ │
│  │  └────────┘                      │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  STATUS CARD                     │ │
│  │  ✅ Adhésion Active              │ │
│  │  Votre adhésion est à jour       │ │
│  └──────────────────────────────────┘ │
│                                        │
│  MES AVANTAGES                         │
│  ┌──────────────┬──────────────┐      │
│  │ 📷 Galerie   │ 📄 Documents │      │
│  ├──────────────┼──────────────┤      │
│  │ 🎉 Événements│ 🎖️ Certificat│      │
│  ├──────────────┼──────────────┤      │
│  │ 👥 Réseau    │ ⭐ Priorité   │      │
│  └──────────────┴──────────────┘      │
│                                        │
│  ACCÈS RAPIDE                          │
│  ┌──────────────────────────────────┐ │
│  │ [Galerie Photos & Vidéos]        │ │
│  └──────────────────────────────────┘ │
│  ┌──────────────────────────────────┐ │
│  │ [Documents Officiels]            │ │
│  └──────────────────────────────────┘ │
│  ┌──────────────────────────────────┐ │
│  │ [Événements & Activités]         │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ 🎖️ TÉLÉCHARGER CERTIFICAT        │ │
│  │   Certificat officiel            │ │
│  │              [Télécharger]       │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ [Voir Mon Profil Complet]        │ │
│  ├──────────────────────────────────┤ │
│  │ [Historique d'Adhésion]          │ │
│  ├──────────────────────────────────┤ │
│  │ [Paramètres]                     │ │
│  └──────────────────────────────────┘ │
│                                        │
└────────────────────────────────────────┘
```

---

## 3. 📜 Layout Structure - MemberHistoryDetailFragment

```
┌────────────────────────────────────────┐
│  AppBarLayout                          │
│  ┌──────────────────────────────────┐ │
│  │  [←] Historique d'Adhésion      │ │
│  └──────────────────────────────────┘ │
├────────────────────────────────────────┤
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ ✏️  DEMANDE SOUMISE              │ │
│  │ 📍 Vous avez soumis...           │ │
│  │ ────────────────────────────────│ │
│  │ 15 Mar 2026 14:30                │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ ✅ DEMANDE APPROUVÉE             │ │
│  │ 📍 Félicitations! Votre...       │ │
│  │ ────────────────────────────────│ │
│  │ 16 Mar 2026 09:00                │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ 🔄 ADHÉSION RENOUVELÉE           │ │
│  │ 📍 Votre adhésion a été...       │ │
│  │ ────────────────────────────────│ │
│  │ 01 Apr 2026 11:30                │ │
│  └──────────────────────────────────┘ │
│                                        │
│  (More items on scroll...)             │
│                                        │
└────────────────────────────────────────┘

Legend:
✏️  = SUBMITTED (Bleu)
✅ = APPROVED (Vert)
❌ = REJECTED (Rouge)
🔄 = RENEWED (Vert)
⏸️  = SUSPENDED (Orange)
⏱️  = EXPIRED (Rouge)
```

---

## 4. ⚙️ Layout Structure - MemberSettingsFragment

```
┌────────────────────────────────────────┐
│  AppBarLayout                          │
│  ┌──────────────────────────────────┐ │
│  │  [←] Paramètres                  │ │
│  └──────────────────────────────────┘ │
├────────────────────────────────────────┤
│                                        │
│  ┌──────────────────────────────────┐ │
│  │   PROFIL UTILISATEUR             │ │
│  │  [Photo] Jean Dupont             │ │
│  │          jean@example.com        │ │
│  └──────────────────────────────────┘ │
│                                        │
│  NOTIFICATIONS                         │
│  ┌──────────────────────────────────┐ │
│  │ Notifications push         [ON]  │ │
│  │ Recevoir notifications...        │ │
│  └──────────────────────────────────┘ │
│  ┌──────────────────────────────────┐ │
│  │ Notifications par email    [ON]  │ │
│  │ Recevoir emails importants...    │ │
│  └──────────────────────────────────┘ │
│                                        │
│  SÉCURITÉ                              │
│  ┌──────────────────────────────────┐ │
│  │ [Changer le mot de passe]        │ │
│  └──────────────────────────────────┘ │
│                                        │
│  À PROPOS                              │
│  ┌──────────────────────────────────┐ │
│  │ [Politique de confidentialité]   │ │
│  ├──────────────────────────────────┤ │
│  │ [Conditions d'utilisation]       │ │
│  ├──────────────────────────────────┤ │
│  │ [À propos de l'app]              │ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ [Se déconnecter] (ROUGE)         │ │
│  └──────────────────────────────────┘ │
│                                        │
└────────────────────────────────────────┘
```

---

## 5. ✏️ Layout Structure - MemberProfileEditFragment

```
┌────────────────────────────────────────┐
│  AppBarLayout                          │
│  ┌──────────────────────────────────┐ │
│  │  [←] Éditer le Profil            │ │
│  └──────────────────────────────────┘ │
├────────────────────────────────────────┤
│                                        │
│        ┌────────────────────┐          │
│        │    [Photo]         │          │
│        │ (120dp x 120dp)    │          │
│        └────────────────────┘          │
│     [Changer la photo]                 │
│                                        │
│  INFORMATIONS PERSONNELLES              │
│  ┌──────────────────────────────────┐ │
│  │  Prénom                          │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ [Texte input]                │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Nom                             │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ [Texte input]                │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Postnom (optionnel)             │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ [Texte input]                │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Email (lecture seule)           │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ jean@example.com (disabled)  │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Téléphone                       │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ [Téléphone input]            │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Adresse                         │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ [Adresse input]              │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Date de naissance [📅]         │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ 01/05/1990                   │ │
│  │  └──────────────────────────────┘ │
│  │                                  │ │
│  │  Profession                      │ │
│  │  ┌──────────────────────────────┐ │
│  │  │ [Profession input]           │ │
│  │  └──────────────────────────────┘ │
│  └──────────────────────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │ [Enregistrer les modifications]  │ │
│  ├──────────────────────────────────┤ │
│  │ [Annuler]                        │ │
│  └──────────────────────────────────┘ │
│                                        │
└────────────────────────────────────────┘
```

---

## 6. 🔄 Data Flow Diagram

```
┌─────────────────┐
│   Fragment      │
│   onViewCreated │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  ViewModel.loadMemberProfile        │
│  ViewModel.loadMemberBenefits       │
│  ViewModel.loadMembershipHistory    │
│  ViewModel.loadMemberStats          │
│  ViewModel.loadNotifications        │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Repository                         │
│  Firebase/Supabase                  │
│  - getMemberProfile(userId)         │
│  - getAvailableMemberBenefits()     │
│  - getMembershipHistory(userId)     │
│  - getNotifications(userId)         │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Backend Database                   │
│  - users table                      │
│  - membership_requests              │
│  - membership_history               │
│  - member_notifications             │
│  - member_benefits                  │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  LiveData emitted                   │
│  - memberProfile                    │
│  - memberBenefits                   │
│  - membershipHistory                │
│  - memberStats                      │
│  - notifications                    │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Fragment observes LiveData         │
│  - displayMemberProfile()           │
│  - displayBenefits()                │
│  - displayHistory()                 │
│  - displayStats()                   │
│  - displayNotifications()           │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  UI Updated                         │
│  All views rendered                 │
└─────────────────────────────────────┘
```

---

## 7. 🎨 Color Palette

```
PRIMARY COLORS
┌─────────────┬──────────┬────────────┐
│ Color       │ Hex      │ Usage      │
├─────────────┼──────────┼────────────┤
│ Primary     │ #1976D2  │ Titles,    │
│ (Blue)      │          │ Actions    │
│ Success     │ #4CAF50  │ ACCEPTED   │
│ (Green)     │          │ Status     │
│ Warning     │ #FF9800  │ PENDING    │
│ (Orange)    │          │ Status     │
│ Error       │ #F44336  │ REJECTED   │
│ (Red)       │          │ Status     │
└─────────────┴──────────┴────────────┘

BENEFIT ICONS COLORS
┌──────────────┬──────────┬─────────────┐
│ Benefit      │ Hex      │ Icon        │
├──────────────┼──────────┼─────────────┤
│ Gallery      │ #2196F3  │ 📷 Blue    │
│ Document     │ #FF6F00  │ 📄 Orange  │
│ Event        │ #E91E63  │ 🎉 Pink    │
│ Certificate  │ #4CAF50  │ 🎖️ Green   │
│ Network      │ #9C27B0  │ 👥 Purple  │
│ Priority     │ #FF5722  │ ⭐ Orange  │
└──────────────┴──────────┴─────────────┘

THEME COLORS
┌──────────────┬──────────┐
│ Surface      │ #FFFFFF  │
│ OnSurface    │ #212121  │
│ SurfaceVar   │ #F5F5F5  │
│ Outline      │ #BDBDBD  │
└──────────────┴──────────┘
```

---

## 8. 📱 Responsive Breakpoints

```
MOBILE PORTRAIT (< 600dp)
┌─────────────────────────┐
│  Width: 100%            │
│  Padding: 16dp          │
│  Grid: 2 columns        │
│  Buttons: Full width    │
│  Hero: Vertical layout  │
└─────────────────────────┘
          ↓
          │ Landscape
          ▼
MOBILE LANDSCAPE (> 600dp, < 900dp)
┌─────────────────────────┐
│  Width: 100%            │
│  Padding: 24dp          │
│  Grid: 3 columns        │
│  Buttons: Side-by-side  │
│  Hero: Horizontal layout│
└─────────────────────────┘
          ↓
          │ Tablet
          ▼
TABLET (> 900dp)
┌─────────────────────────┐
│  Width: 90% (max)       │
│  Padding: 32dp          │
│  Grid: 3+ columns       │
│  Buttons: Smart arrange │
│  Hero: Flexible layout  │
└─────────────────────────┘
```

---

## 9. 📊 Class Diagram (Models)

```
MemberProfile
├─ userId: String
├─ email: String
├─ displayName: String
├─ photoUrl: String
├─ firstname: String
├─ lastname: String
├─ postname: String
├─ membershipStatus: MembershipAdherenceStatus
├─ membershipApprovalDate: Long
├─ subscriptionStatus: String
└─ certificateUrl: String

MemberBenefit
├─ id: String
├─ benefitName: String
├─ description: String
├─ icon: MemberBenefitIcon  (ENUM)
└─ isAccessible: Boolean

MembershipHistory
├─ id: String
├─ userId: String
├─ eventType: String        (ENUM)
├─ description: String
├─ createdBy: String
└─ createdAt: Long

MemberNotification
├─ id: String
├─ userId: String
├─ title: String
├─ message: String
├─ type: String             (ENUM)
├─ isRead: Boolean
├─ actionUrl: String
├─ createdAt: Long
└─ readAt: Long?

MemberStats
├─ memberSinceMonths: Int
├─ totalActivities: Int
├─ eventsAttended: Int
├─ documentsDownloaded: Int
├─ membershipStatus: String
└─ nextRenewalDate: Long
```

---

## 10. 🔌 Integration Points

```
                    ┌──────────────────────────┐
                    │  MemberDashboardFragment │
                    └────────────┬─────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
                ▼                ▼                ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │  nav_gallery │ │nav_documents │ │  nav_events  │
        └──────────────┘ └──────────────┘ └──────────────┘

Integration Points:
├─ GalleryFragment: Add badge "Exclusive Member"
├─ DocumentsFragment: Filter non-member docs
├─ EventsFragment: Show RSVP button
├─ AdminMembershipFragment: Add logging to history
└─ NotificationService: Send membership updates
```

---

## 11. 📈 User Journey Map

```
STEP 1: Login
   │
   ▼
STEP 2: Navigate to Membership
   │
   ▼
STEP 3: See Dashboard (if ACCEPTED)
   ├─ Option A: View Profile → Edit → Save
   │
   ├─ Option B: View History → See Timeline
   │
   ├─ Option C: Access Settings → Change Prefs
   │
   ├─ Option D: Download Certificate
   │
   ├─ Option E: Quick Access to:
   │   ├─ Gallery
   │   ├─ Documents
   │   └─ Events
   │
   └─ Option F: Logout
      │
      ▼
   STEP 4: Redirected to Login
```

---

**Diagrams Version:** 1.0
**Date:** 16 Mars 2026
**Created for:** Apunili ASBL Mobile App


