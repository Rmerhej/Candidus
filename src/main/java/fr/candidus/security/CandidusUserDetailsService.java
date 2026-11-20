package fr.candidus.security;

import fr.candidus.entity.Utilisateur;
import fr.candidus.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Charge un utilisateur depuis la base au moment de la connexion.
 */
@Service
public class CandidusUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CandidusUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public CandidusUser loadUserByUsername(String email) {
        String normalise = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(normalise)
                // Même message que pour un mauvais mot de passe : on ne révèle pas si le compte existe
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants incorrects"));

        return new CandidusUser(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                utilisateur.getPrenom());
    }
}
