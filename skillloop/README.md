# SkillLoop

Banque de temps pour étudiants : une heure d'aide donnée rapporte un crédit-temps, dépensable pour recevoir une heure d'aide d'un autre étudiant.

Projet AWD, validation intermédiaire (séance 4). Thématique : FinTech (monnaie interne, grand livre, séquestre, anti-fraude).

## Équipe

| Membre | Microservice(s) |
| --- | --- |
| Membre 1 | Analytics & Impact  |
| Membre 2 | Skill Catalog & Search |
| Membre 3 | Matching & Recommendation |
| Membre 4 | Session & Scheduling |
| Membre 5 | Wallet & Credit Ledger |
| Membre 6 | Reputation, Trust & Moderation |
| Toute l'équipe | Identity & Profile (service commun : JWT et CRUD utilisateurs)|Notification & Messaging |



## Architecture

Une API Gateway unique, sept microservices (une base par service), un bus Kafka pour les événements. L'authentification est assurée par le service commun Identity, qui émet et signe les JWT (Spring Security, RS256) ; la Gateway et chaque service valident les jetons avec la clé publique (endpoint JWKS).
Diagramme global : `Documentation/Diagrams/architecture/architecture-globale.md`. Interactions par service : `Documentation/Diagrams/architecture/interactions-par-service.md`.

| Service | Base(s) de données | Dossier |
| --- | --- | --- |
| Identity & Profile (commun) | PostgreSQL, Redis | `Backend/identity-service` |
| Skill Catalog & Search | MongoDB, Elasticsearch, MinIO | `Backend/catalog-service` |
| Matching & Recommendation | Neo4j, Redis | `Backend/matching-service` |
| Session & Scheduling | PostgreSQL, Redis | `Backend/session-service` |
| Wallet & Credit Ledger | PostgreSQL | `Backend/wallet-service` |
| Reputation, Trust & Moderation | MongoDB, Redis | `Backend/reputation-service` |
| Analytics & Impact | TimescaleDB, Redis | `Backend/analytics-service` |
| Notification & Messaging | MongoDB, Redis | `Backend/notification-service` |

Infrastructure : `api-gateway`, `discovery-server` (Eureka), `config-server`, `common-lib` (enveloppe d'événements, outbox, sécurité).

## Technologies

Java 21, Spring Boot 3, Spring Security (JWT), Spring Cloud (Gateway, Eureka, Config, OpenFeign), Resilience4j, Apache Kafka, PostgreSQL, TimescaleDB, MongoDB, Elasticsearch, Neo4j, Redis, MinIO, Jitsi Meet, React + TypeScript, Docker Compose, GitHub Actions, Prometheus, Grafana, OpenTelemetry, Zipkin.

## Organisation du dépôt

```text
skillloop-[classe]-[groupe]/
├── Backend/
├── Frontend/
├── Documentation/
│   ├── Presentation/
│   ├── Diagrams/
│   │   ├── architecture/
│   │   ├── classes/
│   │   └── sequence/
│   └── DossierTechnique/
└── README.md
```

## Règles de travail

- Branches : `main` (stable, protégée), `develop` (intégration), `feature/<service>-<fonctionnalité>`.
- Toute fusion dans `develop` passe par une pull request relue par un autre membre.
- Commits au format Conventional Commits (`feat:`, `fix:`, `docs:`).
- Suivi des tâches : GitHub Projects (Kanban), une issue par tâche, un jalon par phase.

## Lancer le projet (cible)

```bash
cd Backend
docker compose up -d        # bases, Kafka
# puis démarrer discovery-server, config-server, api-gateway, puis les services
```

L'environnement complet sera ajouté pendant la phase « Socle technique ».

## Documentation

- Dossier technique : `Documentation/DossierTechnique/`
- Présentation de la séance 4 : `Documentation/Presentation/`
- Diagrammes : `Documentation/Diagrams/`

## Planning

1. Conception (séance 4)
2. Socle technique
3. MVP par service
4. Flux transverses (saga de réservation, crédits, notifications, avis)
5. Fonctionnalités avancées
6. Finalisation et démonstration
