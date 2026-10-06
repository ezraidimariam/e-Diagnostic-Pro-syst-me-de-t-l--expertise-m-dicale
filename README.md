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

## Démarrer l’application

```powershell
cd "C:\Users\maryy\diagnostic"
& "C:\Users\maryy\AppData\Local\Programs\Apache\Maven\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
```

## Notes

- Les tests utilisent une base H2 embarquée.
- Le projet est configuré pour Spring Boot 3.5.16 avec compatibilité Java 25.
- La logique d’authentification est simple et fonctionnelle pour un projet de démonstration ou de test.

## Exemple d’utilisation de l’authentification

Le service `UtilisateurService` permet :
- de créer un utilisateur
- de vérifier les informations de connexion
- de contrôler le statut de l’utilisateur actif/inactif

## Auteurs

Projet créé dans le cadre d’un système de téléexpertise médicale.
