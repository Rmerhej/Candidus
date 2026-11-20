package fr.candidus.dto;

import fr.candidus.entity.Candidature;
import fr.candidus.enums.StatutCandidature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulaire de création / modification d'une candidature.
 * Les champs remplis par l'API ou l'IA (SIREN, analyse, lettre) ne passent pas par ce formulaire.
 */
public class CandidatureForm {

    private Long id;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 150, message = "150 caractères maximum")
    private String entreprise;

    @Size(max = 150, message = "150 caractères maximum")
    private String poste;

    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le lien doit commencer par http:// ou https://")
    @Size(max = 500, message = "500 caractères maximum")
    private String offreUrl;

    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le lien doit commencer par http:// ou https://")
    @Size(max = 255, message = "255 caractères maximum")
    private String siteWeb;

    @Size(max = 100, message = "100 caractères maximum")
    private String ville;

    @Pattern(regexp = "^$|^\\d{5}$", message = "Code postal à 5 chiffres")
    private String codePostal;

    @Size(max = 2000, message = "2000 caractères maximum")
    private String notes;

    @NotNull(message = "Choisissez un statut")
    private StatutCandidature statut = StatutCandidature.A_ENVOYER;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEnvoi;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateRelance;

    public static CandidatureForm fromEntity(Candidature c) {
        CandidatureForm form = new CandidatureForm();
        form.id = c.getId();
        form.entreprise = c.getEntreprise();
        form.poste = c.getPoste();
        form.offreUrl = c.getOffreUrl();
        form.siteWeb = c.getSiteWeb();
        form.ville = c.getVille();
        form.codePostal = c.getCodePostal();
        form.notes = c.getNotes();
        form.statut = c.getStatut();
        form.dateEnvoi = c.getDateEnvoi();
        form.dateRelance = c.getDateRelance();
        return form;
    }

    public void applyTo(Candidature c) {
        c.setEntreprise(entreprise.strip());
        c.setPoste(trimToNull(poste));
        c.setOffreUrl(trimToNull(offreUrl));
        c.setSiteWeb(trimToNull(siteWeb));
        c.setVille(trimToNull(ville));
        c.setCodePostal(trimToNull(codePostal));
        c.setNotes(trimToNull(notes));
        c.setStatut(statut);
        c.setDateEnvoi(dateEnvoi);
        c.setDateRelance(dateRelance);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getOffreUrl() {
        return offreUrl;
    }

    public void setOffreUrl(String offreUrl) {
        this.offreUrl = offreUrl;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public StatutCandidature getStatut() {
        return statut;
    }

    public void setStatut(StatutCandidature statut) {
        this.statut = statut;
    }

    public LocalDate getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDate dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public LocalDate getDateRelance() {
        return dateRelance;
    }

    public void setDateRelance(LocalDate dateRelance) {
        this.dateRelance = dateRelance;
    }
}
