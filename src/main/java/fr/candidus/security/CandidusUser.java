package fr.candidus.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * L'utilisateur connecté, tel que Spring Security le garde en session.
 * Il contient l'identifiant en base : les contrôleurs le récupèrent avec @AuthenticationPrincipal
 * et ne lisent jamais un identifiant d'utilisateur envoyé par le navigateur.
 */
public class CandidusUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String motDePasse;
    private final String prenom;

    public CandidusUser(Long id, String email, String motDePasse, String prenom) {
        this.id = id;
        this.email = email;
        this.motDePasse = motDePasse;
        this.prenom = prenom;
    }

    public Long getId() {
        return id;
    }

    public String getPrenom() {
        return prenom;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
