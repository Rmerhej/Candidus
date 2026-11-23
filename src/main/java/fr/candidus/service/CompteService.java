package fr.candidus.service;

import fr.candidus.entity.Utilisateur;
import fr.candidus.exception.CompteException;
import fr.candidus.repository.CandidatureRepository;
import fr.candidus.repository.ProfilRepository;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * « Mon compte » : changer son mot de passe, supprimer son compte avec toutes ses données.
 */
@Service
public class CompteService {

    /** Mêmes limites que l'inscription (BCrypt ne lit que les 72 premiers caractères). */
    static final int LONGUEUR_MIN = 10;
    static final int LONGUEUR_MAX = 72;

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final CandidatureRepository candidatureRepository;
    private final PasswordEncoder passwordEncoder;

    public CompteService(UtilisateurRepository utilisateurRepository,
                         ProfilRepository profilRepository,
                         CandidatureRepository candidatureRepository,
                         PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.profilRepository = profilRepository;
        this.candidatureRepository = candidatureRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Remplace le mot de passe après avoir vérifié l'ancien.
     *
     * @throws CompteException avec un message lisible si la demande est refusée
     */
    @Transactional
    public void changerMotDePasse(Long utilisateurId, String actuel, String nouveau, String confirmation) {
        Utilisateur utilisateur = charger(utilisateurId);
        if (actuel == null || !passwordEncoder.matches(actuel, utilisateur.getMotDePasse())) {
            throw new CompteException("Le mot de passe actuel est incorrect.");
        }
        if (nouveau == null || nouveau.length() < LONGUEUR_MIN || nouveau.length() > LONGUEUR_MAX) {
            throw new CompteException("Le nouveau mot de passe doit contenir entre " + LONGUEUR_MIN
                    + " et " + LONGUEUR_MAX + " caractères.");
        }
        if (!nouveau.equals(confirmation)) {
            throw new CompteException("Les deux nouveaux mots de passe ne sont pas identiques.");
        }
        if (passwordEncoder.matches(nouveau, utilisateur.getMotDePasse())) {
            throw new CompteException("Le nouveau mot de passe doit être différent de l'ancien.");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveau));
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Supprime définitivement le compte, le profil et toutes les candidatures, après vérification du mot de passe.
     *
     * @throws CompteException si le mot de passe est incorrect
     */
    @Transactional
    public void supprimerCompte(Long utilisateurId, String motDePasse) {
        Utilisateur utilisateur = charger(utilisateurId);
        if (motDePasse == null || !passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            throw new CompteException("Le mot de passe est incorrect.");
        }
        candidatureRepository.deleteByUtilisateurId(utilisateurId);
        profilRepository.deleteByUtilisateurId(utilisateurId);
        utilisateurRepository.delete(utilisateur);
    }

    private Utilisateur charger(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new CompteException("Ce compte n'existe plus."));
    }
}
