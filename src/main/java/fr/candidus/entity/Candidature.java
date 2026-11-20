package fr.candidus.entity;

import fr.candidus.enums.StatutCandidature;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Une candidature (spontanée ou en réponse à une offre). Elle appartient à un seul utilisateur.
 */
@Entity
@Table(name = "candidatures",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_candidatures_utilisateur_siren",
                columnNames = {"utilisateur_id", "siren"}))
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 150)
    private String entreprise;

    @Column(length = 150)
    private String poste;

    @Column(length = 500)
    private String offreUrl;

    @Column(length = 255)
    private String siteWeb;

    @Column(length = 100)
    private String ville;

    @Column(length = 10)
    private String codePostal;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCandidature statut = StatutCandidature.A_ENVOYER;

    private LocalDate dateEnvoi;

    /** Date à laquelle relancer l'employeur s'il n'a pas répondu. */
    private LocalDate dateRelance;

    private Integer score;

    @Column(length = 30)
    private String qualification;

    // ----- Données publiques de l'API Recherche d'entreprises -----

    @Column(length = 9)
    private String siren;

    @Column(length = 10)
    private String codeNaf;

    @Column(length = 2)
    private String trancheEffectif;

    // ----- Analyse et lettre préparées par l'IA -----

    /** 0 à 100 : adéquation entre le profil et l'entreprise. */
    private Integer aiAdequation;

    @Column(length = 2000)
    private String aiAnalyse;

    @Column(length = 300)
    private String aiPosteSuggere;

    @Column(length = 200)
    private String lettreObjet;

    @Column(length = 4000)
    private String lettreCorps;

    private LocalDateTime analysedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = StatutCandidature.A_ENVOYER;
        }
    }

    /** Vrai si la date de relance est atteinte et qu'on attend encore une réponse. */
    public boolean isRelanceDue() {
        return dateRelance != null
                && StatutCandidature.EN_ATTENTE.contains(statut)
                && !dateRelance.isAfter(LocalDate.now());
    }

    /** Initiale de l'entreprise, pour sa pastille dans les listes. */
    public String getInitiale() {
        return fr.candidus.util.Avatar.initiale(entreprise);
    }

    /** Teinte (0 à 3) de la pastille de l'entreprise. */
    public int getTeinte() {
        return fr.candidus.util.Avatar.teinte(entreprise);
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public String getCodeNaf() {
        return codeNaf;
    }

    public void setCodeNaf(String codeNaf) {
        this.codeNaf = codeNaf;
    }

    public String getTrancheEffectif() {
        return trancheEffectif;
    }

    public void setTrancheEffectif(String trancheEffectif) {
        this.trancheEffectif = trancheEffectif;
    }

    public Integer getAiAdequation() {
        return aiAdequation;
    }

    public void setAiAdequation(Integer aiAdequation) {
        this.aiAdequation = aiAdequation;
    }

    public String getAiAnalyse() {
        return aiAnalyse;
    }

    public void setAiAnalyse(String aiAnalyse) {
        this.aiAnalyse = aiAnalyse;
    }

    public String getAiPosteSuggere() {
        return aiPosteSuggere;
    }

    public void setAiPosteSuggere(String aiPosteSuggere) {
        this.aiPosteSuggere = aiPosteSuggere;
    }

    public String getLettreObjet() {
        return lettreObjet;
    }

    public void setLettreObjet(String lettreObjet) {
        this.lettreObjet = lettreObjet;
    }

    public String getLettreCorps() {
        return lettreCorps;
    }

    public void setLettreCorps(String lettreCorps) {
        this.lettreCorps = lettreCorps;
    }

    public LocalDateTime getAnalysedAt() {
        return analysedAt;
    }

    public void setAnalysedAt(LocalDateTime analysedAt) {
        this.analysedAt = analysedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
