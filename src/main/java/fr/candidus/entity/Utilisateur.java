package fr.candidus.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Un compte Candidus. L'adresse e-mail sert d'identifiant de connexion.
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Toujours enregistrée en minuscules : voir setEmail
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    // Mot de passe haché (BCrypt), jamais en clair
    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Column(nullable = false, length = 60)
    private String prenom;

    @Column(nullable = false, length = 60)
    private String nom;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /** "Prénom Nom", utilisé dans la signature des lettres. */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
