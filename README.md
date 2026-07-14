# Certifi — Backend (Spring Boot)

Backend de l'application de signature numérique et de détection d'attaques
assistée par Machine Learning, conforme au prompt maître et à la décision
d'architecture figée : le modèle ML est consommé via l'API du service Python
`ml-service`, jamais chargé directement en Java.

## ⚠️ Limite de l'environnement de génération

Ce backend a été écrit avec le plus grand soin mais **n'a pas pu être compilé
ni testé dans l'environnement où il a été généré**, celui-ci n'ayant pas accès
à Maven Central (uniquement PyPI et npm sont accessibles). Avant toute mise en
production :

```bash
mvn clean verify
```

doit être exécuté dans un environnement avec un accès réseau standard, pour
confirmer la compilation et faire passer les tests unitaires fournis
(`CryptoServiceTest`). Signalez-moi tout écart si la compilation révèle des
ajustements nécessaires — je les corrigerai.

## Stack

- Spring Boot 3.3 / Java 21
- Spring Web (REST), Spring Security (JWT), Bean Validation
- Spring Data JPA + PostgreSQL, migrations Flyway
- `spring-webflux` (WebClient seul, sans serveur réactif) pour appeler `ml-service`
- Cryptographie : `java.security` uniquement (RSA-2048, SHA-256, RSASSA-PSS) — aucune dépendance crypto tierce
- Spring Mail pour les alertes email

## Démarrage local

```bash
# 1. Base de données
docker run -d --name certifi-db -e POSTGRES_DB=certifi \
  -e POSTGRES_USER=certifi -e POSTGRES_PASSWORD=certifi \
  -p 5432:5432 postgres:16-alpine

# 2. ml-service doit tourner sur http://localhost:8000 (voir ../ml-service/README.md)

# 3. Backend
mvn spring-boot:run
```

Un compte administrateur de démonstration est créé par la migration
`V2__seed_admin_user.sql` : `admin@enspd.example` / `ChangeMe123!`
**(à supprimer avant toute mise en production).**

## Décisions d'architecture reprises du prompt maître

- **Séparation stricte crypto / ML** : `CryptoService` établit seul la validité
  mathématique de la signature ; `MlClassifierClient` ne fait que qualifier la
  nature de l'anomalie. `DocumentVerificationService` orchestre les deux sans
  jamais les confondre dans la réponse.
- **Résilience au service ML** : `MlClassifierHttpClient` a un timeout court
  (800 ms par défaut) et retourne `Optional.empty()` en cas d'échec — la
  vérification cryptographique continue de répondre normalement
  (`mlServiceUnavailable: true` dans la réponse), et une alerte
  `ML_SERVICE_DOWN` est levée pour l'administrateur.
- **Modularité** : `crypto/`, `ml/`, `service/`, `web/` sont des packages
  strictement séparés, conformément à l'exigence non fonctionnelle n°4 du
  mémoire (Tableau 2.2).

## Limites connues de ce prototype (héritées du mémoire, section 4.6.2)

- Une seule paire de clés de confiance (`KeyManagementService`) : la
  substitution de clé n'est pas vérifiée cryptographiquement dans cette
  version (le champ `keyMatch` est toujours `true`). Une évolution vers une
  PKI multi-signataires (certificats X.509) est nécessaire pour un scénario
  réel de substitution de clé.
- La clé privée est stockée en clair sur le disque du serveur
  (`./keys/certifi-private.key`). **À ne jamais faire en production** — utiliser
  un HSM ou un coffre-fort de secrets (Vault, AWS KMS...).
- Pas d'autorité d'horodatage certifiée (RFC 3161) : la détection de rejeu
  s'appuie sur l'historique en base, pas sur un nonce cryptographique.

## Arborescence

```
src/main/java/com/enspd/certifi/
  config/       Sécurité, CORS
  security/     JWT (génération, filtre, UserDetailsService)
  domain/       Entités JPA, enums, repositories
  dto/          Contrats request/response (alignés avec le frontend)
  crypto/       Hash SHA-256, signature/vérification RSA-PSS, gestion des clés
  ml/           Client HTTP vers ml-service, extraction de features
  service/      Orchestration métier (signature, vérification, alertes, historique)
  web/          Contrôleurs REST
  exception/    Gestion centralisée des erreurs
src/main/resources/
  application.yml
  db/migration/ Scripts Flyway
```
