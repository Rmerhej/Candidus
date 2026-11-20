package fr.candidus.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Les étapes d'une candidature, dans l'ordre du suivi.
 */
public enum StatutCandidature {

    A_ENVOYER("À envoyer", "a-envoyer"),
    ENVOYEE("Envoyée", "envoyee"),
    RELANCEE("Relancée", "relancee"),
    ENTRETIEN("Entretien", "entretien"),
    REFUS("Refus", "refus"),
    EMBAUCHE("Embauche", "embauche");

    /** Candidatures parties chez l'employeur, quelle que soit la suite. */
    public static final Set<StatutCandidature> ENVOYEES = EnumSet.of(ENVOYEE, RELANCEE, ENTRETIEN, REFUS, EMBAUCHE);

    /** L'employeur a répondu (positivement ou non). */
    public static final Set<StatutCandidature> REPONSES = EnumSet.of(ENTRETIEN, REFUS, EMBAUCHE);

    /** Une relance a du sens : on attend encore une réponse. */
    public static final Set<StatutCandidature> EN_ATTENTE = EnumSet.of(ENVOYEE, RELANCEE);

    private final String libelle;
    private final String couleur;

    StatutCandidature(String libelle, String couleur) {
        this.libelle = libelle;
        this.couleur = couleur;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Rang dans la barre d'étapes de la fiche (1 à 4) :
     * À envoyer, Envoyée, Relancée, puis Réponse (entretien, refus ou embauche).
     */
    public int getEtape() {
        return switch (this) {
            case A_ENVOYER -> 1;
            case ENVOYEE -> 2;
            case RELANCEE -> 3;
            case ENTRETIEN, REFUS, EMBAUCHE -> 4;
        };
    }

    /** Suffixe de la classe CSS du badge (badge-...). */
    public String getCouleur() {
        return couleur;
    }
}
