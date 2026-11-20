# Candidus

**L'assistant emploi français, simple et pas cher.**
Candidus aide les demandeurs d'emploi à suivre leurs candidatures, à trouver des
entreprises qui recrutent près de chez eux (même sans offre publiée) et à préparer
avec l'IA des lettres et des relances qu'ils relisent avant tout envoi.

Site : https://candidus.app (en préparation)

## État du projet

Version 1 en cours (MVP) :

- Comptes utilisateurs : inscription, connexion par e-mail, mots de passe hachés (BCrypt).
  Chaque utilisateur ne voit que ses propres données.
- Profil paramétrable : métier visé, type d'entreprises, départements, résumé du CV,
  points forts, signature.
- Suivi des candidatures : À envoyer → Envoyée → Relancée → Entretien / Refus / Embauche,
  avec rappel de relance automatique 7 jours après l'envoi.
- Recherche d'entreprises par mots-clés et départements (API Recherche d'entreprises),
  pour les candidatures spontanées.
- Lettre préparée par l'IA à partir du profil, relue et envoyée par l'utilisateur lui-même.

À venir : application installable (PWA), abonnements (Stripe), pages légales (CGU, RGPD),
suppression du compte, limites d'usage de l'IA par abonnement.

## Lancer le projet en local

1. Copier `.env.example` en `.env` et choisir ses mots de passe.
2. `docker compose up -d --build`
3. Ouvrir http://localhost:8081

4. Cliquer sur « Créer mon compte ».

MySQL écoute sur le port 3309 et l'application sur le port 8081 : Candidus peut
tourner en même temps que d'autres projets.

## Technique

Java 21, Spring Boot, Spring Security, Spring Data JPA, Flyway, MySQL, Thymeleaf,
Bootstrap, Spring AI (Gemini), API publique Recherche d'entreprises.

© 2026 Roland Merhej. Tous droits réservés (voir COPYRIGHT.md).
