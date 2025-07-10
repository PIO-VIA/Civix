# Plateforme de Vote Électronique

## Description

Système de vote électronique sécurisé développé avec Spring Boot, offrant une solution complète pour organiser des élections en ligne. La plateforme garantit la transparence, la sécurité et l'intégrité du processus électoral tout en maintenant une interface utilisateur intuitive.

## Caractéristiques Principales

- **Vote sécurisé** : Un vote unique par électeur avec vérifications multiples
- **Interface multi-rôles** : Portails distincts pour électeurs, administrateurs et consultation publique
- **Résultats temps réel** : Suivi en direct des résultats avec statistiques détaillées
- **Gestion des campagnes** : Système complet de gestion des candidats et leurs campagnes
- **Rapports avancés** : Génération de rapports détaillés avec export CSV
- **Monitoring système** : Surveillance complète de la plateforme
- **Documentation API** : Interface Swagger complète

## Architecture Technique

### Stack Technologique

- **Backend** : Spring Boot 3.5.3
- **Base de données** : PostgreSQL 15+
- **Sécurité** : Spring Security avec BCrypt
- **Documentation** : SpringDoc OpenAPI 3 (Swagger)
- **Validation** : Jakarta Validation
- **Mapping** : MapStruct
- **Email** : Spring Mail
- **Build** : Maven

### Architecture Applicative

```
├── Controllers     # Couche présentation (REST API)
├── Services        # Logique métier
├── Repositories    # Accès aux données (JPA)
├── Models          # Entités JPA
├── DTOs            # Objets de transfert
├── Mappers         # Conversion entités/DTOs
├── Configuration   # Configuration Spring
└── Utils           # Utilitaires
```

## Installation et Configuration

### Prérequis

- Java 21+
- PostgreSQL 15+
- Maven 3.8+
- Compte email SMTP (Gmail recommandé)

### Configuration de la Base de Données

1. Créer une base de données PostgreSQL :
```sql
CREATE DATABASE vote;
```

2. Configurer `application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/vote
spring.datasource.username=votre_utilisateur
spring.datasource.password=votre_mot_de_passe
```

### Configuration Email

Configurer les paramètres SMTP dans `application.properties` :
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-app
```

Note : Pour Gmail, utiliser un mot de passe d'application généré dans les paramètres de sécurité.

### Démarrage

1. Cloner le repository :
```bash
git clone <repository-url>
cd electronicvoting
```

2. Installer les dépendances :
```bash
./mvnw clean install
```

3. Lancer l'application :
```bash
./mvnw spring-boot:run
```

4. Accéder à l'application :
- Application : http://localhost:8080
- Documentation API : http://localhost:8080/swagger-ui.html

## Initialisation

### Création du Premier Administrateur

Avant d'utiliser la plateforme, créer le premier administrateur :

```bash
POST /api/setup/first-admin
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@example.com",
  "motDePasse": "Admin123!",
  "empreinteDigitale": null
}
```

## Guide d'Utilisation

### Pour les Administrateurs

1. **Connexion** : `POST /api/auth/admin/login`
2. **Gestion électeurs** : Création via `POST /api/admin/electeurs`
3. **Gestion candidats** : CRUD via `/api/admin/candidats`
4. **Gestion campagnes** : CRUD via `/api/admin/campagnes`
5. **Tableau de bord** : `GET /api/admin/dashboard`
6. **Rapports** : `/api/reports/*`

### Pour les Électeurs

1. **Réception identifiants** : Par email après création par admin
2. **Connexion** : `POST /api/auth/electeur/login`
3. **Consultation candidats** : `GET /api/electeur/candidats`
4. **Vote** : `POST /api/votes/effectuer`
5. **Résultats** : `GET /api/electeur/resultats`

### Consultation Publique

- **Accueil** : `GET /api/public/accueil`
- **Candidats** : `GET /api/public/candidats`
- **Campagnes** : `GET /api/public/campagnes`
- **Résultats temps réel** : `GET /api/public/resultats-temps-reel`

## Documentation API

### Endpoints Principaux

#### Authentification
- `POST /api/auth/electeur/login` - Connexion électeur
- `POST /api/auth/admin/login` - Connexion administrateur
- `POST /api/auth/electeur/change-password` - Changement mot de passe

#### Vote
- `POST /api/votes/effectuer` - Effectuer un vote
- `GET /api/votes/statut` - Vérifier statut de vote
- `GET /api/votes/resultats` - Consulter résultats
- `GET /api/votes/statistiques` - Statistiques de vote

#### Administration
- `POST /api/admin/electeurs` - Créer électeur
- `POST /api/admin/candidats` - Créer candidat
- `POST /api/admin/campagnes` - Créer campagne
- `GET /api/admin/dashboard` - Tableau de bord

#### Public
- `GET /api/public/accueil` - Page d'accueil
- `GET /api/public/candidats` - Liste candidats
- `GET /api/public/campagnes` - Liste campagnes

### Authentification

L'API utilise des tokens Bearer pour l'authentification :

```bash
Authorization: Bearer <token>
```

Durée de vie des tokens : 24 heures

## Sécurité

### Mesures de Sécurité Implémentées

- **Hachage des mots de passe** : BCrypt avec facteur 15
- **Validation des entrées** : Jakarta Validation
- **Prévention double vote** : Vérifications multiples
- **Tokens sécurisés** : Génération avec UUID + timestamp
- **CORS configuré** : Protection contre les attaques cross-origin
- **Validation email** : Format et unicité

### Processus de Vote Sécurisé

1. Vérification identité électeur
2. Contrôle unicité du vote
3. Validation candidat existant
4. Enregistrement transactionnel
5. Mise à jour statut électeur
6. Logs d'audit

## Structure du Projet

```
src/
├── main/
│   ├── java/com/personnal/electronicvoting/
│   │   ├── config/          # Configuration Spring
│   │   ├── controller/      # Contrôleurs REST
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Gestion des erreurs
│   │   ├── mapper/         # MapStruct mappers
│   │   ├── model/          # Entités JPA
│   │   ├── repository/     # Repositories JPA
│   │   ├── service/        # Services métier
│   │   └── util/           # Classes utilitaires
│   └── resources/
│       └── application.properties
└── test/
    └── java/               # Tests unitaires
```

### Modèle de Données

#### Entités Principales

- **Electeur** : Utilisateurs autorisés à voter
- **Candidat** : Candidats à l'élection
- **Vote** : Enregistrement des votes
- **Campagne** : Campagnes électorales
- **Administrateur** : Administrateurs système

#### Relations

- Un électeur peut avoir un vote (1:0..1)
- Un candidat peut avoir plusieurs votes (1:N)
- Un candidat peut avoir plusieurs campagnes (1:N)
- Contraintes d'unicité sur votes par électeur

## Monitoring et Maintenance

### Health Checks

- `GET /api/system/health` - État général du système
- `GET /api/system/metrics` - Métriques détaillées

### Maintenance

- `POST /api/system/maintenance` - Mode maintenance
- `POST /api/system/backup` - Sauvegarde données
- `POST /api/system/cleanup` - Nettoyage système

### Logs

L'application génère des logs détaillés pour :
- Connexions utilisateurs
- Opérations de vote
- Erreurs système
- Actions administratives

## Rapports

### Types de Rapports

- **Résultats complets** : Résultats détaillés avec analyses
- **Participation** : Analyse de la participation électorale
- **Candidats** : Performance des candidats
- **Campagnes** : Efficacité des campagnes
- **Exécutif** : Rapport consolidé pour direction

### Formats d'Export

- JSON (API)
- CSV (téléchargement)
- Rapports formatés

## Tests

### Tests Manuels

Utiliser Swagger UI pour tester les endpoints :
http://localhost:8080/swagger-ui.html

### Tests d'Intégration

1. Créer administrateur
2. Créer électeurs et candidats
3. Effectuer votes
4. Vérifier résultats
5. Générer rapports

## Déploiement

### Variables d'Environnement

```bash
# Base de données
DB_URL=jdbc:postgresql://host:port/database
DB_USERNAME=username
DB_PASSWORD=password

# Email
EMAIL_USERNAME=email@domain.com
EMAIL_PASSWORD=app-password

# Sécurité
JWT_SECRET=your-secret-key
```



### Production

Recommandations pour la production :
- Utiliser HTTPS
- Configurer un reverse proxy (Nginx)
- Sauvegardes régulières de la base
- Monitoring des performances
- Logs centralisés

## Contribution

### Standards de Code

- Suivre les conventions Java
- Documenter les API avec Swagger
- Écrire des tests unitaires
- Logs appropriés avec SLF4J

### Workflow

1. Fork du repository
2. Création branche feature
3. Développement avec tests
4. Pull request avec description

## Résolution de Problèmes

### Problèmes Courants

**Erreur de connexion base de données**
- Vérifier PostgreSQL démarré
- Contrôler credentials dans application.properties

**Emails non envoyés**
- Vérifier configuration SMTP
- Tester avec `/api/test/email`

**Token invalide**
- Vérifier format Bearer token
- Contrôler expiration (24h)

### Support

Pour des questions techniques :
1. Consulter la documentation Swagger
2. Vérifier les logs applicatifs
3. Utiliser les endpoints de health check

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

## Auteurs

Développé par moi dans le cadre d'un projet d'apprentissage des technologies Spring Boot et des systèmes de vote électronique sécurisés.

