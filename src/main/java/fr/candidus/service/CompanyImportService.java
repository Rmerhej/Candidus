package fr.candidus.service;

import fr.candidus.client.RechercheEntreprisesClient;
import fr.candidus.client.dto.ApiCompany;
import fr.candidus.client.dto.ApiEtablissement;
import fr.candidus.client.dto.ApiSearchResponse;
import fr.candidus.dto.CompanyCandidate;
import fr.candidus.dto.CompanySearchResult;
import fr.candidus.dto.ImportReport;
import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import fr.candidus.enums.StatutCandidature;
import fr.candidus.enums.TrancheEffectif;
import fr.candidus.repository.CandidatureRepository;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Recherche d'entreprises (selon le profil de l'utilisateur) et import des entreprises choisies
 * comme candidatures spontanées "à envoyer".
 */
@Service
public class CompanyImportService {

    private static final Pattern SIREN_PATTERN = Pattern.compile("^\\d{9}$");

    private final RechercheEntreprisesClient client;
    private final CandidatureRepository candidatureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatureScoringService scoringService;

    public CompanyImportService(RechercheEntreprisesClient client,
                                CandidatureRepository candidatureRepository,
                                UtilisateurRepository utilisateurRepository,
                                CandidatureScoringService scoringService) {
        this.client = client;
        this.candidatureRepository = candidatureRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.scoringService = scoringService;
    }

    /** Une page de résultats, en signalant les entreprises que l'utilisateur a déjà importées. */
    public CompanySearchResult search(Long utilisateurId, Profil profil, int page) {
        int safePage = clampPage(page);
        String requete = requeteEffective(profil);
        ApiSearchResponse response = client.search(requete, profil.getDepartementsList(), safePage);
        List<ApiCompany> companies = response == null ? List.of() : response.safeResults();

        Set<String> existing = findExisting(utilisateurId, companies.stream().map(ApiCompany::siren).toList());

        List<CompanyCandidate> candidates = companies.stream()
                .map(company -> toCandidate(company, existing.contains(company.siren())))
                .toList();

        int totalPages = response == null || response.totalPages() == null ? 0 : response.totalPages();
        int totalResults = response == null || response.totalResults() == null ? 0 : response.totalResults();

        return new CompanySearchResult(
                candidates,
                safePage,
                Math.min(totalPages, RechercheEntreprisesClient.MAX_PAGE),
                totalResults,
                requete);
    }

    /**
     * Importe les entreprises cochées.
     * On ne fait pas confiance aux données renvoyées par le navigateur : on relit la même page
     * dans l'API et on ne garde que les SIREN cochés. Seuls les SIREN viennent du formulaire.
     */
    @Transactional
    public ImportReport importCompanies(Long utilisateurId, Profil profil, int page,
                                        Collection<String> selectedSirens) {

        Set<String> wanted = selectedSirens == null ? Set.of() : selectedSirens.stream()
                .filter(siren -> siren != null && SIREN_PATTERN.matcher(siren).matches())
                .collect(Collectors.toSet());

        if (wanted.isEmpty()) {
            return new ImportReport(0, 0, 0);
        }

        ApiSearchResponse response = client.search(requeteEffective(profil), profil.getDepartementsList(),
                clampPage(page));
        List<ApiCompany> companies = response == null ? List.of() : response.safeResults();

        List<ApiCompany> selected = companies.stream()
                .filter(company -> wanted.contains(company.siren()))
                .toList();

        Set<String> existing = findExisting(utilisateurId, selected.stream().map(ApiCompany::siren).toList());

        List<Candidature> toSave = new ArrayList<>();
        for (ApiCompany company : selected) {
            if (!existing.contains(company.siren())) {
                Candidature candidature = toCandidature(company, profil);
                candidature.setUtilisateur(utilisateurRepository.getReferenceById(utilisateurId));
                toSave.add(candidature);
            }
        }
        candidatureRepository.saveAll(toSave);

        int notFound = wanted.size() - selected.size();
        return new ImportReport(toSave.size(), selected.size() - toSave.size(), notFound);
    }

    /** Mots trop courants pour servir seuls à une recherche. */
    private static final Set<String> MOTS_VIDES = Set.of(
            "organisme", "organismes", "entreprise", "entreprises", "societe", "société", "cabinet",
            "centre", "centres", "maison", "service", "services", "agence", "groupe");

    /**
     * L'API cherche surtout dans les NOMS des entreprises : « organisme de formation » ne trouve
     * presque rien, alors que « formation » trouve des milliers d'entreprises.
     * Si la phrase complète ne donne aucun résultat, on garde son mot le plus parlant.
     * (Une seule requête de vérification, page 1, pour décider.)
     */
    String requeteEffective(Profil profil) {
        String motsCles = profil.getMotsCles() == null ? "" : profil.getMotsCles().strip();
        String[] mots = motsCles.split("\\s+");
        if (mots.length < 2) {
            return motsCles;
        }
        ApiSearchResponse essai = client.search(motsCles, profil.getDepartementsList(), 1);
        if (essai != null && essai.totalResults() != null && essai.totalResults() > 0) {
            return motsCles;
        }
        return java.util.Arrays.stream(mots)
                .map(String::strip)
                .filter(m -> m.length() >= 4)
                .filter(m -> !MOTS_VIDES.contains(m.toLowerCase(java.util.Locale.ROOT)))
                .max(java.util.Comparator.comparingInt(String::length))
                .orElse(motsCles);
    }

    // ----- Conversions -----

    private CompanyCandidate toCandidate(ApiCompany company, boolean alreadyImported) {
        ApiEtablissement siege = company.siege();
        return new CompanyCandidate(
                company.siren(),
                company.nomComplet(),
                siege == null ? null : siege.libelleCommune(),
                siege == null ? null : siege.codePostal(),
                company.activitePrincipale(),
                company.trancheEffectif(),
                TrancheEffectif.libelle(company.trancheEffectif()),
                alreadyImported);
    }

    Candidature toCandidature(ApiCompany company, Profil profil) {
        ApiEtablissement siege = company.siege();

        Candidature candidature = new Candidature();
        candidature.setSiren(company.siren());
        candidature.setEntreprise(truncate(company.nomComplet() == null ? "Entreprise " + company.siren()
                : company.nomComplet(), 150));
        candidature.setVille(truncate(siege == null ? null : siege.libelleCommune(), 100));
        candidature.setCodePostal(siege == null ? null : truncate(siege.codePostal(), 10));
        candidature.setCodeNaf(company.activitePrincipale());
        candidature.setTrancheEffectif(company.trancheEffectif());
        candidature.setStatut(StatutCandidature.A_ENVOYER);
        candidature.setNotes("Candidature spontanée. Entreprise trouvée dans l'Annuaire des entreprises ("
                + TrancheEffectif.libelle(company.trancheEffectif()) + ").");

        scoringService.appliquer(candidature, profil);
        return candidature;
    }

    // ----- Outils -----

    private Set<String> findExisting(Long utilisateurId, List<String> sirens) {
        List<String> valid = sirens.stream().filter(s -> s != null && !s.isBlank()).toList();
        // Une clause "IN ()" vide n'est pas acceptée par toutes les bases : on évite la requête
        return valid.isEmpty() ? Set.of() : candidatureRepository.findExistingSirens(utilisateurId, valid);
    }

    private static int clampPage(int page) {
        return Math.min(Math.max(page, 1), RechercheEntreprisesClient.MAX_PAGE);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
