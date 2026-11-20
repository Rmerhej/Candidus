package fr.candidus.ai;

import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;

/**
 * Analyse d'une candidature par une IA, à partir du profil du candidat.
 * Le reste de l'application ne dépend que de cette interface : changer de fournisseur d'IA
 * ne demandera qu'une nouvelle implémentation.
 */
public interface CandidatureAnalyzer {

    /**
     * @throws AiUnavailableException si l'IA n'est pas configurée ou ne répond pas
     */
    AiAnalysis analyze(Candidature candidature, Profil profil);
}
