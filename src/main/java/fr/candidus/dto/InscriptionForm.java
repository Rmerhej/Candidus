package fr.candidus.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulaire de création de compte.
 */
public class InscriptionForm {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 60, message = "60 caractères maximum")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 60, message = "60 caractères maximum")
    private String nom;

    @NotBlank(message = "L'adresse e-mail est obligatoire")
    @Email(message = "Adresse e-mail invalide")
    @Size(max = 190, message = "190 caractères maximum")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 10, max = 72, message = "Entre 10 et 72 caractères")
    private String motDePasse;

    @NotBlank(message = "Confirmez le mot de passe")
    private String confirmation;

    @AssertTrue(message = "Vous devez accepter les conditions d'utilisation")
    private boolean accepteConditions;

    /** Règle vérifiée en plus des annotations : les deux mots de passe doivent être identiques. */
    @AssertTrue(message = "Les deux mots de passe ne correspondent pas")
    public boolean isConfirmationIdentique() {
        return motDePasse == null || motDePasse.equals(confirmation);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    public boolean isAccepteConditions() {
        return accepteConditions;
    }

    public void setAccepteConditions(boolean accepteConditions) {
        this.accepteConditions = accepteConditions;
    }
}
