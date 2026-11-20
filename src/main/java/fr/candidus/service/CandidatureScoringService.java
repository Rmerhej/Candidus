package fr.candidus.service;

import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Score "règles simples" (0 à 100) : aide à choisir par où commencer.
 *
 * <pre>
 * Entreprise dans un département du profil              30
 * Taille : 10 salariés et plus = 20, 3 à 9 = 10          20
 * Poste précis renseigné                                 20
 * Offre publiée (lien vers l'annonce)                    20
 * Site web connu (pour personnaliser la lettre)          10
 *                                                       ---
 *                                                       100
 * </pre>
 */
@Service
public class CandidatureScoringService {

    /** Tranches INSEE de 10 salariés et plus : entreprises qui recrutent plus souvent. */
    private static final Set<String> DIX_ET_PLUS = Set.of(
            "11", "12", "21", "22", "31", "32", "41", "42", "51", "52", "53");

    /** Tranches de 3 à 9 salariés. */
    private static final Set<String> TROIS_A_NEUF = Set.of("02", "03");

    public int calculerScore(Candidature candidature, Profil profil) {
        int score = 0;

        if (dansLaZone(candidature.getCodePostal(), profil)) {
            score += 30;
        }

        String tranche = candidature.getTrancheEffectif();
        if (tranche != null && DIX_ET_PLUS.contains(tranche)) {
            score += 20;
        } else if (tranche != null && TROIS_A_NEUF.contains(tranche)) {
            score += 10;
        }

        if (estRempli(candidature.getPoste())) {
            score += 20;
        }
        if (estRempli(candidature.getOffreUrl())) {
            score += 20;
        }
        if (estRempli(candidature.getSiteWeb())) {
            score += 10;
        }
        return score;
    }

    public String qualification(int score) {
        if (score >= 70) {
            return "Prioritaire";
        }
        if (score >= 40) {
            return "À étudier";
        }
        return "Faible potentiel";
    }

    /** Calcule et enregistre le score dans la candidature. */
    public void appliquer(Candidature candidature, Profil profil) {
        int score = calculerScore(candidature, profil);
        candidature.setScore(score);
        candidature.setQualification(qualification(score));
    }

    /** Vrai si le code postal commence par l'un des départements du profil (ex : 78400 pour 78). */
    static boolean dansLaZone(String codePostal, Profil profil) {
        if (codePostal == null || profil == null) {
            return false;
        }
        for (String departement : profil.getDepartementsList()) {
            if (codePostalDuDepartement(codePostal, departement)) {
                return true;
            }
        }
        return false;
    }

    /** Gère aussi la Corse (2A/2B : codes postaux 200xx et 201xx-202xx) et l'outre-mer (971...). */
    static boolean codePostalDuDepartement(String codePostal, String departement) {
        String dep = departement.toUpperCase(java.util.Locale.ROOT);
        if (dep.equals("2A") || dep.equals("2B")) {
            return codePostal.startsWith("20");
        }
        return codePostal.startsWith(dep);
    }

    private static boolean estRempli(String value) {
        return value != null && !value.isBlank();
    }
}
