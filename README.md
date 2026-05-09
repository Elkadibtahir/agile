# 🚀 TeamTrack v2 — Guide de démarrage complet

## Prérequis
- Java 21
- Maven 3.8+
- SQL Server + SSMS
- IntelliJ IDEA (recommandé)
- Un compte Gmail (pour les emails)

---

## Étape 1 — Base de données

Ouvre **SQL Server Management Studio**, connecte-toi et exécute le fichier `database.sql`.

Ce script crée :
- La base `TeamTrackDB`
- Les 5 tables (users, projects, tasks, comments, project_members)
- Les contraintes CHECK sur status/priority/role/sentiment
- Un trigger `updated_at` sur les tâches
- 5 index pour les requêtes fréquentes
- Une vue `v_project_stats` pour le monitoring
- Un compte admin de test

**Compte de test :**
- Username : `admin`
- Password : `Admin123!`

---

## Étape 2 — Configuration

Dans `src/main/resources/application.properties` :

```properties
# Ton mot de passe SQL Server
spring.datasource.password=TonMotDePasse

# Ton email Gmail
spring.mail.username=tonmail@gmail.com
spring.mail.password=ton_app_password   ← App Password Google (pas ton vrai mdp)
app.mail.from=TeamTrack <tonmail@gmail.com>
```

### Comment générer un App Password Gmail :
1. Va sur myaccount.google.com → Sécurité
2. Active la validation en 2 étapes
3. Cherche "Mots de passe des applications"
4. Génère un mot de passe pour "Mail" → copie les 16 caractères

---

## Étape 3 — Lancer

```bash
mvn spring-boot:run
```

Ou dans IntelliJ : clic droit sur `TeamTrackApplication.java` → Run

Ouvre : `http://localhost:8080`

---

## Architecture complète

```
src/main/java/com/teamtrack/
│
├── TeamTrackApplication.java      ← @SpringBootApplication + @EnableAsync
│
├── config/
│   └── SecurityConfig.java        ← Login/logout + CSRF cookie + BCrypt 12
│
├── entity/                        ← Tables JPA (5 entités)
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   ├── Comment.java
│   └── ProjectMember.java
│
├── enums/                         ← Types fixes
│   ├── TaskStatus.java            (TODO, IN_PROGRESS, DONE)
│   ├── TaskPriority.java          (LOW, MEDIUM, HIGH, CRITICAL)
│   └── MemberRole.java            (OWNER, DEVELOPER, TESTER, VIEWER)
│
├── dto/
│   └── RegisterDTO.java           ← Validation mot de passe (@Valid)
│
├── model/                         ← POJOs non-JPA
│   ├── Statistics.java            ← Stats calculées du projet
│   └── Alert.java                 ← Alerte prédictive de retard
│
├── repository/                    ← JpaRepository (accès BDD)
│   ├── UserRepository.java
│   ├── ProjectRepository.java     ← + requête paginée
│   ├── TaskRepository.java        ← + requête paginée + countCompletedSince
│   ├── CommentRepository.java
│   └── ProjectMemberRepository.java
│
├── service/                       ← Logique métier
│   ├── AiService.java             ← Analyse de sentiment (mots-clés)
│   ├── StatisticsService.java     ← Calcul stats + alertes prédictives
│   ├── EmailService.java          ← 5 types d'emails (@Async)
│   ├── UserService.java           ← Register avec RegisterDTO
│   ├── ProjectService.java        ← CRUD + membres + pagination
│   ├── TaskService.java           ← CRUD + statut + pagination + emails
│   ├── CommentService.java        ← Ajout + email notification
│   └── CustomUserDetailsService.java ← Spring Security login
│
├── controller/                    ← Routes HTTP
│   ├── AuthController.java        ← /auth/login, /auth/register (@Valid)
│   ├── DashboardController.java   ← /dashboard + / redirect
│   ├── ProjectController.java     ← /projects/** + pagination
│   └── TaskController.java        ← /projects/{id}/tasks/**
│
└── exception/
    └── GlobalExceptionHandler.java ← @ControllerAdvice (400/404/500)
```

---

## Fonctionnalités v2

| Fonctionnalité | Détail |
|---|---|
| 🔐 Auth sécurisée | Login/Register/Logout + BCrypt strength 12 |
| ✅ Validation | Mot de passe : 8+ chars, 1 majuscule, 1 chiffre |
| 🛡️ CSRF | Token dans chaque formulaire + header pour fetch() |
| 🚫 Error handling | Page 400/404/500 conviviale (GlobalExceptionHandler) |
| 📄 Pagination | 9 projets/page, 10 tâches/page (configurable) |
| 📧 Emails (5 types) | Bienvenue, Assignation, Statut, Commentaire, Alerte retard |
| 📊 Statistics | Progression %, vélocité, comptage par statut |
| ⚠️ Alert prédictive | ON_TRACK / WARNING / AT_RISK selon vélocité |
| 🤖 AiService | Sentiment : POSITIVE / NEUTRAL / NEGATIVE |
| 📋 Kanban | Drag & drop avec CSRF, 3 colonnes |
| 👥 Membres | Ajout/retrait avec rôles |
| 💬 Commentaires | Sur les tâches avec analyse sentiment |

---

## Emails envoyés automatiquement

| Événement | Destinataire |
|---|---|
| Inscription | Nouvel utilisateur |
| Tâche créée avec assigné | L'assigné |
| Assigné changé | Nouvel assigné |
| Statut tâche changé | L'assigné |
| Nouveau commentaire | L'assigné de la tâche |
| Alerte retard (manuel) | Le owner du projet |

