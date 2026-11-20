package fr.candidus.service;

import fr.candidus.dto.ProfilForm;
import fr.candidus.entity.Profil;
import fr.candidus.entity.Utilisateur;
import fr.candidus.repository.ProfilRepository;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lecture et mise à jour du profil de l'utilisateur connecté.
 */
@Service
public class ProfilService {

    private final ProfilRepository profilRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ProfilService(ProfilRepository profilRepository, UtilisateurRepository utilisateurRepository) {
        this.profilRepository = profilRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Le profil de l'utilisateur, créé vide s'il n'existe pas encore. */
    @Transactional
    public Profil getOrCreate(Long utilisateurId) {
        return profilRepository.findByUtilisateurId(utilisateurId).orElseGet(() -> {
            Utilisateur utilisateur = utilisateurRepository.getReferenceById(utilisateurId);
            Profil profil = new Profil();
            profil.setUtilisateur(utilisateur);
            return profilRepository.save(profil);
        });
    }

    @Transactional
    public void update(Long utilisateurId, ProfilForm form) {
        Profil profil = getOrCreate(utilisateurId);
        form.applyTo(profil);
        profilRepository.save(profil);
    }
}
