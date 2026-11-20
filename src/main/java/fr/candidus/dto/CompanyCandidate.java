package fr.candidus.dto;

/**
 * Une entreprise trouvée par l'API, affichée dans la page d'import.
 *
 * @param alreadyImported vrai si ce SIREN est déjà dans vos candidatures
 */
public record CompanyCandidate(
        String siren,
        String name,
        String city,
        String postalCode,
        String codeNaf,
        String trancheEffectif,
        String trancheLibelle,
        boolean alreadyImported
) {

    public String initiale() {
        return fr.candidus.util.Avatar.initiale(name);
    }

    public int teinte() {
        return fr.candidus.util.Avatar.teinte(name);
    }
}
