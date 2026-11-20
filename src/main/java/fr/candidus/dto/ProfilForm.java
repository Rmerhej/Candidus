package fr.candidus.dto;

import fr.candidus.entity.Profil;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulaire "Mon profil". Tous les champs sont facultatifs : on peut le remplir petit à petit.
 */
public class ProfilForm {

    @Size(max = 150, message = "150 caractères maximum")
    private String metierVise;

    @Size(max = 200, message = "200 caractères maximum")
    private String motsCles;

    // Ex : "78, 92" ou "75 2A 971"
    @Pattern(regexp = "^\\s*$|^\\s*(\\d{2,3}|2[ABab])(\\s*[,;\\s]\\s*(\\d{2,3}|2[ABab]))*\\s*$",
            message = "Indiquez des numéros de départements séparés par des virgules (ex : 78, 92)")
    @Size(max = 60, message = "60 caractères maximum")
    private String departements;

    @Size(max = 3000, message = "3000 caractères maximum")
    private String cvResume;

    @Size(max = 1000, message = "1000 caractères maximum")
    private String pointsForts;

    @Pattern(regexp = "^$|^[0-9+ .()-]{6,30}$", message = "Numéro de téléphone invalide")
    private String telephone;

    @Pattern(regexp = "^$|^https?://\\S+$", message = "Le lien doit commencer par http:// ou https://")
    @Size(max = 255, message = "255 caractères maximum")
    private String lienProfil;

    public static ProfilForm fromEntity(Profil profil) {
        ProfilForm form = new ProfilForm();
        form.metierVise = profil.getMetierVise();
        form.motsCles = profil.getMotsCles();
        form.departements = profil.getDepartements();
        form.cvResume = profil.getCvResume();
        form.pointsForts = profil.getPointsForts();
        form.telephone = profil.getTelephone();
        form.lienProfil = profil.getLienProfil();
        return form;
    }

    public void applyTo(Profil profil) {
        profil.setMetierVise(trimToNull(metierVise));
        profil.setMotsCles(trimToNull(motsCles));
        profil.setDepartements(normaliserDepartements(departements));
        profil.setCvResume(trimToNull(cvResume));
        profil.setPointsForts(trimToNull(pointsForts));
        profil.setTelephone(trimToNull(telephone));
        profil.setLienProfil(trimToNull(lienProfil));
    }

    /** "78 ;92, 2a" devient "78,92,2A". */
    static String normaliserDepartements(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return String.join(",", java.util.Arrays.stream(value.split("[,;\\s]+"))
                .map(String::strip)
                .filter(code -> !code.isEmpty())
                .map(code -> code.toUpperCase(java.util.Locale.ROOT))
                .distinct()
                .toList());
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public String getMetierVise() {
        return metierVise;
    }

    public void setMetierVise(String metierVise) {
        this.metierVise = metierVise;
    }

    public String getMotsCles() {
        return motsCles;
    }

    public void setMotsCles(String motsCles) {
        this.motsCles = motsCles;
    }

    public String getDepartements() {
        return departements;
    }

    public void setDepartements(String departements) {
        this.departements = departements;
    }

    public String getCvResume() {
        return cvResume;
    }

    public void setCvResume(String cvResume) {
        this.cvResume = cvResume;
    }

    public String getPointsForts() {
        return pointsForts;
    }

    public void setPointsForts(String pointsForts) {
        this.pointsForts = pointsForts;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getLienProfil() {
        return lienProfil;
    }

    public void setLienProfil(String lienProfil) {
        this.lienProfil = lienProfil;
    }
}
