package fr.candidus.dto;

import fr.candidus.entity.Candidature;

import java.util.List;

/**
 * Chiffres du tableau de bord, prêts à afficher.
 *
 * @param tauxReponse pourcentage de candidatures envoyées qui ont reçu une réponse,
 *                    ou null si aucune n'a encore été envoyée
 */
public record DashboardStats(
        long total,
        long envoyees,
        long reponses,
        long entretiens,
        Integer tauxReponse,
        List<CountItem> parStatut,
        List<Candidature> relancesDues
) {

    /** Une ligne de répartition : libellé, nombre, pourcentage du total et couleur du badge. */
    public record CountItem(String libelle, long nombre, int pourcentage, String couleur) {
    }
}
