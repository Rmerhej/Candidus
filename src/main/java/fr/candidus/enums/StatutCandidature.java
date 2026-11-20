package fr.candidus.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Les étapes d'une candidature, dans l'ordre du suivi.
 */
public enum StatutCandidature {

    A_ENVOYER("À envoyer", "secondary"),
    ENVOYEE("Envoyée", "primary"),
    RELANCEE("Relancée", "info"),
    ENTRETIEN("Entretien", "warning"),
    REFUS("Refus", "danger"),
    EMBAUCHE("Embauche", "success");

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

    /** Couleur Bootstrap du badge (text-bg-...). */
    public String getCouleur() {
        return couleur;
    }
}
