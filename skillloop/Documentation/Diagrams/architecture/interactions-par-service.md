# Interactions par service

Exigence du cours : chaque étudiant justifie au moins une communication REST, un événement publié et un événement consommé.

## Service commun : Identity & Profile (toute l'équipe)

Périmètre : authentification JWT et CRUD des utilisateurs.

| Méthode | Endpoint | Rôle | Codes |
| --- | --- | --- | --- |
| POST | /auth/register | Créer un compte (email universitaire) | 201, 400, 409 |
| POST | /auth/login | Obtenir access token (court) et refresh token | 200, 401, 403 |
| POST | /auth/refresh | Renouveler l'access token | 200, 401 |
| POST | /auth/logout | Révoquer le refresh token | 204 |
| GET | /.well-known/jwks.json | Clé publique pour valider les JWT | 200 |
| GET | /users | Lister les utilisateurs (admin) | 200 |
| GET | /users/{id} | Lire un utilisateur | 200, 404 |
| POST | /users | Créer un utilisateur (admin) | 201, 400, 409 |
| PUT | /users/{id} | Modifier un utilisateur | 200, 400, 403, 404 |
| PATCH | /users/{id}/roles | Changer les rôles (admin) | 200, 403, 404 |
| POST | /users/{id}/suspend | Suspendre (modérateur) | 204, 404 |
| DELETE | /users/{id} | Supprimer un utilisateur | 202, 403, 404 |
| GET | /users/me | Lire son profil | 200 |

Le JWT contient : `sub` (userId), `roles`, `exp`, `iss`. Signé en RS256 ; la Gateway et chaque service le valident avec la clé publique.
Événements publiés : UserRegistered, UserVerified, UserSuspended, UserDeleted. Consommé : UserSuspensionRequested.

## Services individuels

| Service | REST sortant (synchrone) | Événement publié | Événement consommé |
| --- | --- | --- | --- |
| Catalog | Identity : vérifier le statut et le rôle du propriétaire avant publication | SkillPublished | UserSuspended |
| Matching | Catalog : annonces candidates ; Identity : disponibilités | BarterCycleDetected (nouveau) | SkillPublished |
| Session | Wallet : geler les crédits ; Identity : disponibilités | SessionConfirmed | DisputeResolved |
| Wallet | Identity : vérifier que le compte est actif avant un séquestre | CreditsReserved | SessionCompleted |
| Reputation | Session : vérifier que l'auteur a participé à la séance avant d'accepter un avis | ReviewPosted | SessionCompleted |
| Analytics | Identity : résoudre le campus d'un utilisateur | LowEngagementDetected | SessionCompleted |
| Notification | Identity : lire l'email et la langue du destinataire | NotificationFailed (nouveau) | BarterCycleDetected, SessionConfirmed, CreditsReleased, etc. |

Éléments marqués « nouveau » : ajoutés pour satisfaire l'exigence « publier au moins un événement » (Matching et Notification ne publiaient rien) et « au moins un appel REST » (Catalog, Wallet, Reputation et Notification n'en émettaient aucun).
