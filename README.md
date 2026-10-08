# Télémédecine / Système de téléexpertise médicale

Ce projet est une application Java Spring Boot pour gérer un système de téléexpertise médicale.

## Objectif

Le système permet de gérer :
- les utilisateurs
- les médecins
- les patients
- les créneaux disponibles
- les consultations
- les demandes d’expertise
- les admissions

## Démarrage local

Prérequis : Java 25 et Maven 3.9 ou supérieur. Depuis la racine :

```powershell
mvn spring-boot:run
```

Ouvrir ensuite `http://localhost:8080`. À la première ouverture, créer le compte administrateur. Les comptes suivants sont créés par l'administrateur connecté. Les données locales sont stockées dans `./data/teleexpertise`.

L'interface utilise une session HTTP et un jeton CSRF pour les requêtes qui modifient les données. L'API renvoie JSON et les routes principales sont :

| Ressource | Routes |
| --- | --- |
| Authentification | `GET /api/auth/status`, `POST /api/auth/bootstrap`, `POST /api/auth/login`, `POST /api/auth/logout`, `POST /api/auth/users` |
| Patients | `GET/POST /api/patients`, `GET/PUT/DELETE /api/patients/{id}` |
| Médecins | `GET/POST /api/medecins`, `GET/PUT/DELETE /api/medecins/{id}`, filtre `?specialite=...` |
| Consultations | `GET/POST /api/consultations`, `GET/PUT/DELETE /api/consultations/{id}` |
| Créneaux | `GET /api/creneaux?medecinId={id}&disponibles=true`, `POST /api/creneaux`, `POST/DELETE /api/creneaux/{id}/reservation` |
| Admissions | `GET/POST /api/admissions`, `GET /api/admissions/{id}`, `PUT /api/admissions/{id}/statut` |
| Téléexpertises | `GET/POST /api/demandes-expertise`, `GET /api/demandes-expertise/{id}`, `POST /api/demandes-expertise/{id}/reponse`, `POST /api/demandes-expertise/{id}/annulation` |

Les requêtes POST/PUT/DELETE vers les ressources métier doivent inclure le cookie de session et l'en-tête `X-CSRF-TOKEN`, obtenu via `GET /api/auth/status`.

## Stack technique

- Java 25
- Spring Boot 3.5.16
- Spring Data JPA
- H2 Database (pour les tests)
- Maven
- JUnit 5

## Structure du projet

```text
src/
├── main/
│   └── java/
│       └── fr/
│           └── teleexpertise/
│               ├── dao/
│               ├── entity/
│               ├── service/
│               └── TeleexpertiseApplication.java
└── test/
    └── java/
        └── fr/
            └── teleexpertise/
                └── service/
```

## Lancer les tests

Depuis le dossier du projet :

```powershell
cd "C:\Users\maryy\diagnostic"
& "C:\Users\maryy\AppData\Local\Programs\Apache\Maven\apache-maven-3.9.11\bin\mvn.cmd" test
```

Pour un test ciblé :

```powershell
cd "C:\Users\maryy\diagnostic"
& "C:\Users\maryy\AppData\Local\Programs\Apache\Maven\apache-maven-3.9.11\bin\mvn.cmd" test -Dtest=CreneauServiceTest
```

## Notes

- H2 fichier est configuré pour le développement local; les tests utilisent une base H2 temporaire isolée.
- Le projet est configuré pour Spring Boot 3.5.16 avec compatibilité Java 25.
- Cette version est un prototype local : elle ne remplace pas les contrôles réglementaires, la journalisation d'accès, le chiffrement en transit/au repos, ni une politique d'autorisations médicales par rôle. Ne pas y stocker de données réelles de patients avant une revue sécurité et conformité.
- La téléconsultation vidéo, les notifications, la facturation et l'interopérabilité avec des systèmes hospitaliers ne sont pas implémentées.

## Auteurs

Projet créé dans le cadre d’un système de téléexpertise médicale.
