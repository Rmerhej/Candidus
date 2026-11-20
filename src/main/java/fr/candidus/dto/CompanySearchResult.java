package fr.candidus.dto;

import java.util.List;

/**
 * Une page de résultats de l'API, prête à afficher.
 *
 * @param page numéro de page à partir de 1 (convention de l'API)
 */
public record CompanySearchResult(
        List<CompanyCandidate> candidates,
        int page,
        int totalPages,
        int totalResults,
        String requete
) {

    /** Vrai si la recherche a été élargie à un seul mot faute de résultat avec la phrase complète. */
    public boolean elargie(String motsCles) {
        return requete != null && motsCles != null && !requete.equalsIgnoreCase(motsCles.strip());
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
