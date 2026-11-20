package fr.candidus.service;

import fr.candidus.dto.CandidatureForm;
import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import fr.candidus.enums.StatutCandidature;
import fr.candidus.exception.CandidatureNotFoundException;
import fr.candidus.repository.CandidatureRepository;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Gestion des candidatures de l'utilisateur connecté.
 * Chaque méthode reçoit l'identifiant de l'utilisateur : impossible de lire ou modifier
 * la candidature d'un autre, même en changeant le numéro dans l'adresse.
 */
@Service
public class CandidatureService {

    /** Délai avant de relancer un employeur qui n'a pas répondu. */
    public static final int JOURS_AVANT_RELANCE = 7;

    static final int MAX_PAGE_SIZE = 50;

    private final CandidatureRepository candidatureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilService profilService;
    private final CandidatureScoringService scoringService;
    private final Clock clock;

    public CandidatureService(CandidatureRepository candidatureRepository,
                              UtilisateurRepository utilisateurRepository,
                              ProfilService profilService,
                              CandidatureScoringService scoringService,
                              Clock clock) {
        this.candidatureRepository = candidatureRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.profilService = profilService;
        this.scoringService = scoringService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Candidature find(Long utilisateurId, Long id) {
        return candidatureRepository.findByIdAndUtilisateurId(id, utilisateurId)
                .orElseThrow(() -> new CandidatureNotFoundException(id));
    }

    /** Liste paginée, filtrée par statut et par texte (entreprise, poste ou ville). */
    @Transactional(readOnly = true)
    public Page<Candidature> search(Long utilisateurId, StatutCandidature statut, String texte, int page, int size) {
        Specification<Candidature> spec = (root, query, cb) ->
                cb.equal(root.get("utilisateur").get("id"), utilisateurId);

        if (statut != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statut"), statut));
        }
        if (texte != null && !texte.isBlank()) {
            String motif = "%" + texte.strip().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.<String>get("entreprise")), motif, '\\'),
                    cb.like(cb.lower(root.<String>get("poste")), motif, '\\'),
                    cb.like(cb.lower(root.<String>get("ville")), motif, '\\')));
        }

        // Les paramètres viennent de l'URL : on les borne
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort tri = Sort.by(Sort.Order.desc("score"), Sort.Order.desc("id"));

        return candidatureRepository.findAll(spec, PageRequest.of(safePage, safeSize, tri));
    }

    /** Crée ou modifie une candidature, puis recalcule son score. */
    @Transactional
    public Candidature save(Long utilisateurId, CandidatureForm form) {
        Candidature candidature;
        if (form.getId() == null) {
            candidature = new Candidature();
            candidature.setUtilisateur(utilisateurRepository.getReferenceById(utilisateurId));
        } else {
            candidature = find(utilisateurId, form.getId());
        }
        form.applyTo(candidature);
        completerDates(candidature);

        Profil profil = profilService.getOrCreate(utilisateurId);
        scoringService.appliquer(candidature, profil);
        return candidatureRepository.save(candidature);
    }

    @Transactional
    public void delete(Long utilisateurId, Long id) {
        candidatureRepository.delete(find(utilisateurId, id));
    }

    /** "Je l'ai envoyée" : date d'envoi aujourd'hui, relance prévue dans 7 jours. */
    @Transactional
    public void marquerEnvoyee(Long utilisateurId, Long id) {
        Candidature candidature = find(utilisateurId, id);
        LocalDate aujourdhui = LocalDate.now(clock);
        candidature.setStatut(StatutCandidature.ENVOYEE);
        candidature.setDateEnvoi(aujourdhui);
        candidature.setDateRelance(aujourdhui.plusDays(JOURS_AVANT_RELANCE));
        candidatureRepository.save(candidature);
    }

    /** "J'ai relancé" : nouvelle relance prévue dans 7 jours. */
    @Transactional
    public void marquerRelancee(Long utilisateurId, Long id) {
        Candidature candidature = find(utilisateurId, id);
        candidature.setStatut(StatutCandidature.RELANCEE);
        candidature.setDateRelance(LocalDate.now(clock).plusDays(JOURS_AVANT_RELANCE));
        candidatureRepository.save(candidature);
    }

    /** Si l'utilisateur indique une date d'envoi sans date de relance, on la calcule pour lui. */
    void completerDates(Candidature candidature) {
        if (StatutCandidature.EN_ATTENTE.contains(candidature.getStatut())) {
            if (candidature.getDateEnvoi() == null) {
                candidature.setDateEnvoi(LocalDate.now(clock));
            }
            if (candidature.getDateRelance() == null) {
                candidature.setDateRelance(candidature.getDateEnvoi().plusDays(JOURS_AVANT_RELANCE));
            }
        }
    }
}
