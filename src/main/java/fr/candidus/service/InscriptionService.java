package fr.candidus.service;

import fr.candidus.dto.InscriptionForm;
import fr.candidus.entity.Profil;
import fr.candidus.entity.Utilisateur;
import fr.candidus.exception.EmailDejaUtiliseException;
import fr.candidus.repository.ProfilRepository;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Création d'un compte : l'utilisateur et son profil (vide au départ).
 */
@Service
public class InscriptionService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final PasswordEncoder passwordEncoder;

    public InscriptionService(UtilisateurRepository utilisateurRepository,
                              ProfilRepository profilRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.profilRepository = profilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @throws EmailDejaUtiliseException si l'adresse est déjà prise
     */
    @Transactional
    public Utilisateur inscrire(InscriptionForm form) {
        String email = form.getEmail().strip().toLowerCase(Locale.ROOT);
        if (utilisateurRepository.existsByEmail(email)) {
            throw new EmailDejaUtiliseException();
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setPrenom(form.getPrenom().strip());
        utilisateur.setNom(form.getNom().strip());
        utilisateur.setMotDePasse(passwordEncoder.encode(form.getMotDePasse()));

        try {
            // saveAndFlush : si deux inscriptions arrivent en même temps avec le même e-mail,
            // la contrainte d'unicité de la base est vérifiée ici, et non à la fin de la transaction
            utilisateur = utilisateurRepository.saveAndFlush(utilisateur);
        } catch (DataIntegrityViolationException e) {
            throw new EmailDejaUtiliseException();
        }

        Profil profil = new Profil();
        profil.setUtilisateur(utilisateur);
        profilRepository.save(profil);

        return utilisateur;
    }
}
