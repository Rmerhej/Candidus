package fr.candidus.dto;

import fr.candidus.entity.Candidature;

import java.util.List;

/**
 * Chiffres du tableau de bord, prêts à afficher.
 *
 * @param tauxReponse      pourcentage de candidatures envoyées qui ont reçu une réponse,
 *                         ou null si aucune n'a encore été envoyée
 * @param envoyeesSemaine  candidatures envoyées depuis lundi
 * @param objectifSemaine  objectif hebdomadaire (5 par défaut)
 */
public record DashboardStats(
        long total,
        long envoyees,
        long reponses,
        long entretiens,
        Integer tauxReponse,
        List<CountItem> parStatut,
        List<Candidature> relancesDues,
        long envoyeesSemaine,
        int objectifSemaine
) {

    /** Une case par candidature de l'objectif : true si elle est faite. */
    public List<Boolean> getPasSemaine() {
        return java.util.stream.IntStream.rangeClosed(1, objectifSemaine)
                .mapToObj(i -> i <= envoyeesSemaine)
                .toList();
    }

    /** Message d'encouragement selon l'avancement de la semaine. */
    public String getEncouragement() {
        long reste = objectifSemaine - envoyeesSemaine;
        if (envoyeesSemaine == 0) {
            return "Une première candidature cette semaine ?";
        }
        if (reste <= 0) {
            return "Objectif atteint, bravo !";
        }
        return reste == 1 ? "Plus qu'une, vous y êtes presque !" : "Plus que " + reste + ", continuez comme ça !";
    }

    /** Une ligne de répartition : libellé, nombre, pourcentage du total et couleur du badge. */
    public record CountItem(String libelle, long nombre, int pourcentage, String couleur) {
    }
}
