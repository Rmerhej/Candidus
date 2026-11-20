package fr.candidus.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Le profil de recherche d'un utilisateur : c'est lui qui adapte Candidus à chacun.
 * Il sert à chercher des entreprises, à calculer les scores et à guider l'IA.
 */
@Entity
@Table(name = "profils")
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    /** Ex : "Formateur pour adultes en développement web". */
    @Column(length = 150)
    private String metierVise;

    /** Mots envoyés à l'API Recherche d'entreprises, ex : "organisme de formation". */
    @Column(length = 200)
    private String motsCles;

    /** Codes de départements séparés par des virgules, ex : "78,92". */
    @Column(length = 60)
    private String departements;

    @Column(length = 3000)
    private String cvResume;

    @Column(length = 1000)
    private String pointsForts;

    @Column(length = 30)
    private String telephone;

    /** Lien LinkedIn ou portfolio, ajouté à la signature. */
    @Column(length = 255)
    private String lienProfil;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Les codes de départements, nettoyés (ex : ["78", "92"]). */
    public List<String> getDepartementsList() {
        if (departements == null || departements.isBlank()) {
            return List.of();
        }
        return Arrays.stream(departements.split("[,;\\s]+"))
                .map(String::strip)
                .filter(code -> !code.isEmpty())
                .distinct()
                .toList();
    }

    /** Vrai quand le profil contient le minimum pour chercher des entreprises. */
    public boolean isPretPourRecherche() {
        return motsCles != null && !motsCles.isBlank() && !getDepartementsList().isEmpty();
    }

    public Long getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
