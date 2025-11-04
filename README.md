![screenshot](docs/banner.png)

# 🌞 Daily Joy — Les petits plaisirs de ta journée

---

## ✨ Concept

**Daily Joy** est une application Android développée avec **Jetpack Compose** et **Material 3**, qui t’invite à tirer chaque jour une ou plusieurs cartes représentant tes **plaisirs du jour** 🎉  
L’idée : s’accorder un moment rien qu’à soi, sans culpabilité, avec une expérience ludique et colorée.

---

## 🪄 Fonctionnalités principales

| 🌟 Fonction | 🧩 Détails |
|--------------|------------|
| **Tirage de carte** | Animation de carte retournée pour découvrir un plaisir du jour |
| **Plaisirs personnalisables** | Liste préremplie + ajout des tiens (Food, Sport, Divertissement, etc.) |
| **Modes configurables** | Choisis ton rythme : 1-3 plaisirs par jour ou 7-21 par semaine |
| **Historique hebdomadaire** | Visualise tes plaisirs accomplis dans une timeline animée |
| **Notifications** | Rappel quotidien avec message motivant 💬 |
| **Thème fun & moderne** | Design Material 3 dynamique, coloré et fluide, mode clair/sombre automatique |

---

## 🧱 Stack technique

| Domaine | Outils |
|----------|--------|
| Langage | **Kotlin (100%)** |
| UI | **Jetpack Compose**, **Material 3**, **MotionLayout**, **Lottie Compose** |
| Architecture | **MVVM + Clean Architecture** |
| Navigation | **Compose Navigation** |
| State management | **StateFlow / MutableState** |
| DI | **Hilt** |
| Images | **Coil** |
| Data | **Room / DataStore Preferences** |
| Build | **Gradle Kotlin DSL** |

### 🔥 Firestore – Espace Communauté

| Chemin                                             | Description |
|----------------------------------------------------| --- |
| `public_profiles/{userId}`                         | Profil public (username, handle, avatarUrl, bio, stats). |
| `users/{uid}/friends/{friendId}`                   | Relation d'amitié (`since`). |
| `users/{uid}/friend_requests_received/{requestId}` | Demandes reçues (RequestDto). |
| `users/{uid}/friend_requests_sent/{requestId}`     | Demandes envoyées. |
| `users/{uid}/suggestions/{userId}`                 | Suggestions personnalisées. |
| `users/{uid}/feed/{postId}`                        | Flux personnalisé (PostDto + sous-collections `comments`, `likes`). |
| `posts/{postId}`                                   | Référence globale pour réactions/commentaires rapides. |

Sous-collections : `comments/{commentId}` (CommentDto), `likes/{userId}`.

Indexes recommandés :

```bash
firebase firestore:indexes:create \
  --collection-group=public_profiles --query-scope=COLLECTION --fields=username:ASC,handle:ASC

firebase firestore:indexes:create \
  --collection-group=feed --query-scope=COLLECTION --fields=timestamp:DESC

firebase firestore:indexes:create \
  --collection-group=comments --query-scope=COLLECTION --fields=timestamp:ASC
```

---

## ⚙️ Structure du projet (à venir)

---

## 🖼️ Screenshots (à venir)

---

## 🚀 Installation

```bash
git clone https://github.com/xNarCiik/Daily-Joy.git
cd DailyJoy
./gradlew assembleDebug
```

---

## 👨‍💻 Auteur

👋 Damien — Développeur mobile freelance

“J’aime créer des expériences visuelles qui inspirent autant qu’elles impressionnent.”

[📫 LinkedIn](https://www.linkedin.com/in/damien-legagnoux)
