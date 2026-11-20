# Candidus

**L'assistant emploi français, simple et pas cher.**
Candidus aide les demandeurs d'emploi à suivre leurs candidatures, à trouver des
entreprises qui recrutent près de chez eux (même sans offre publiée) et à préparer
avec l'IA des lettres et des relances qu'ils relisent avant tout envoi.

Site : https://candidus.app (en préparation)

## État du projet

Étape 1 : base technique reprise d'AI-Prospect-Hunter et renommée.
Prochaines étapes : comptes utilisateurs, profil paramétrable (métier, zone, CV),
suivi des candidatures et relances.

## Lancer le projet en local

1. Copier `.env.example` en `.env` et choisir ses mots de passe.
2. `docker compose up -d --build`
3. Ouvrir http://localhost:8081

MySQL écoute sur le port 3309 et l'application sur le port 8081 : Candidus peut
tourner en même temps qu'AI-Prospect-Hunter.

## Technique

Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway, MySQL, Thymeleaf,
Bootstrap, Spring AI (Gemini), API publique Recherche d'entreprises.

© 2026 Roland Merhej. Tous droits réservés (voir COPYRIGHT.md).
