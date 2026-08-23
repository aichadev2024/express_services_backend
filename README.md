# Express Services - API Backend 🚀

Backend API REST Java Spring Boot pour la plateforme de gestion de livraisons et logistique **Express Services Mali**.

## 📌 Présentation

Ce service backend gère l'ensemble des opérations métiers de la plateforme Express Services :
- **Authentification & Sécurité** : JWT (JSON Web Token), rôles (ADMIN, LIVREUR, PARTENAIRE, CLIENT), gestion des profils et sécurité des accès.
- **Gestion des Commandes** : Création, attribution aux livreurs, suivi des statuts, gestion des motifs d'annulation, notifications WhatsApp & email.
- **Gestion de la Flotte & Quartiers** : Référence des tarifs de livraison par quartier à Bamako et dans les régions du Mali.
- **Tableau de Bord & Métriques** : API d'agrégation de statistiques pour le tableau de bord administrateur et partenaire.

---

## 🛠️ Stack Technique

- **Langage & Framework** : Java 21 / Spring Boot 3.x
- **Sécurité** : Spring Security & JWT Authentication
- **Base de Données** : PostgreSQL (avec Flyway pour les migrations de schéma)
- **Emailing & Messaging** : Brevo API (SMTP/HTTPS) & Integration WhatsApp
- **Build System** : Maven

---

## 🚀 Démarrage Rapide

### 1. Prérequis
- Java JDK 21 ou supérieur
- PostgreSQL 16 ou supérieur
- Maven 3.8+

### 2. Configuration de la Base de Données
Créez la base de données PostgreSQL locale :
```sql
CREATE DATABASE expressdb;
```

### 3. Configuration des Variables d'Environnement
Vous pouvez configurer les variables suivantes ou utiliser un fichier `.env` :

| Variable | Description | Valeur par défaut |
|---|---|---|
| `DATABASE_URL` | URL de la BD PostgreSQL | `jdbc:postgresql://localhost:5432/expressdb` |
| `DB_USERNAME` | Utilisateur PostgreSQL | `postgres` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | `postgres` |
| `JWT_SECRET` | Clé secrète pour signer les JWT | *(Secrète)* |
| `BREVO_API_KEY` | Clé d'API Brevo / Sendinblue pour l'envoi d'emails | *(Clé API)* |

### 4. Compilation & Lancement

```bash
# Compilation du projet
mvn clean compile

# Lancement de l'application
mvn spring-boot:run
```
L'API sera disponible sur : `http://localhost:8080`

---

## 📁 Structure du Projet

```text
express_services_backend/
├── src/main/java/com/expressservices/
│   ├── auth/         # Authentification, JWT, Sécurité
│   ├── commande/     # Logique métier des commandes & motifs d'annulation
│   ├── utilisateur/  # Gestion des comptes (Livreurs, Clients, Partenaires)
│   ├── quartier/     # Grille tarifaire & zones de livraison
│   └── shared/       # Exceptions, DTOs partagés, utils
└── src/main/resources/
    ├── application.yaml  # Configuration de l'application
    └── db/migration/     # Scripts de migration Flyway
```

---

## 📄 Licence

Ce projet est la propriété exclusive d'**Express Services Mali**. Tous droits réservés.
