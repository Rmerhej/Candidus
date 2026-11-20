package fr.candidus.ai;

/**
 * Réponse structurée attendue de l'IA.
 * Spring AI demande au modèle de répondre en JSON avec exactement ces champs,
 * puis convertit ce JSON en record Java (méthode .entity(AiAnalysis.class)).
 *
 * @param adequation    0 à 100 : adéquation entre le profil du candidat et l'entreprise
 * @param analyse       3 à 5 phrases qui expliquent la note
 * @param posteSuggere  le poste le plus plausible pour ce candidat dans cette entreprise
 * @param objetLettre   objet de l'e-mail de candidature
 * @param corpsLettre   texte de la candidature (sans signature : elle est ajoutée automatiquement)
 */
public record AiAnalysis(
        int adequation,
        String analyse,
        String posteSuggere,
        String objetLettre,
        String corpsLettre
) {
}
