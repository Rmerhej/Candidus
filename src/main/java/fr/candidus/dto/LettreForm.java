package fr.candidus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lettre de candidature relue et corrigée par l'utilisateur.
 */
public class LettreForm {

    @NotBlank(message = "L'objet est obligatoire")
    @Size(max = 200, message = "200 caractères maximum")
    private String subject;

    @NotBlank(message = "Le texte de la lettre est obligatoire")
    @Size(max = 3800, message = "3800 caractères maximum")
    private String body;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
