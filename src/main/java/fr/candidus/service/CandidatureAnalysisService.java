package fr.candidus.service;

import fr.candidus.ai.AiAnalysis;
import fr.candidus.ai.CandidatureAnalyzer;
import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import fr.candidus.entity.Utilisateur;
import fr.candidus.repository.CandidatureRepository;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Prépare une candidature avec l'IA (analyse + lettre) et enregistre la lettre corrigée.
 * La lettre n'est JAMAIS envoyée automatiquement : l'utilisateur la relit et l'envoie lui-même.
 */
@Service
public class CandidatureAnalysisService {

    private final CandidatureAnalyzer analyzer;
    private final CandidatureService candidatureService;
    private final CandidatureRepository candidatureRepository;
    private final ProfilService profilService;
    private final UtilisateurRepository utilisateurRepository;

    public CandidatureAnalysisService(CandidatureAnalyzer analyzer,
                                      CandidatureService candidatureService,
                                      CandidatureRepository candidatureRepository,
                                      ProfilService profilService,
                                      UtilisateurRepository utilisateurRepository) {
        this.analyzer = analyzer;
        this.candidatureService = candidatureService;
        this.candidatureRepository = candidatureRepository;
        this.profilService = profilService;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Volontairement SANS @Transactional : l'appel à l'IA peut durer plusieurs secondes,
     * on ne garde pas une connexion à la base ouverte pendant ce temps.
     */
    public Candidature preparer(Long utilisateurId, Long candidatureId) {
        Candidature candidature = candidatureService.find(utilisateurId, candidatureId);
        Profil profil = profilService.getOrCreate(utilisateurId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElseThrow();

        AiAnalysis analysis = analyzer.analyze(candidature, profil);

        candidature.setAiAdequation(Math.clamp(analysis.adequation(), 0, 100));
        candidature.setAiAnalyse(truncate(analysis.analyse(), 2000));
        candidature.setAiPosteSuggere(truncate(analysis.posteSuggere(), 300));
        candidature.setLettreObjet(truncate(analysis.objetLettre(), 200));
        candidature.setLettreCorps(truncate(
                analysis.corpsLettre().strip() + "\n\n" + signature(utilisateur, profil), 4000));
        candidature.setAnalysedAt(LocalDateTime.now());

        return candidatureRepository.save(candidature);
    }

    /** Enregistre la lettre après les corrections de l'utilisateur. */
    public void enregistrerLettre(Long utilisateurId, Long candidatureId, String objet, String corps) {
        Candidature candidature = candidatureService.find(utilisateurId, candidatureId);
        candidature.setLettreObjet(objet.strip());
        candidature.setLettreCorps(corps.strip());
        candidatureRepository.save(candidature);
    }

    /** "Cordialement, Prénom Nom", puis téléphone et lien s'ils sont renseignés. */
    static String signature(Utilisateur utilisateur, Profil profil) {
        StringBuilder sb = new StringBuilder("Cordialement,\n").append(utilisateur.getNomComplet());
        if (profil.getTelephone() != null && !profil.getTelephone().isBlank()) {
            sb.append("\n").append(profil.getTelephone());
        }
        if (profil.getLienProfil() != null && !profil.getLienProfil().isBlank()) {
            sb.append("\n").append(profil.getLienProfil());
        }
        return sb.toString();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
