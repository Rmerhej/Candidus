package fr.candidus.web;

import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import fr.candidus.entity.Utilisateur;
import fr.candidus.enums.StatutCandidature;
import fr.candidus.repository.CandidatureRepository;
import fr.candidus.repository.ProfilRepository;
import fr.candidus.repository.UtilisateurRepository;
import fr.candidus.security.CandidusUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Parcours d'un utilisateur connecté, et surtout : chacun ne voit que SES candidatures.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CandidatureWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    private CandidusUser alice;
    private CandidusUser bob;

    @BeforeEach
    void setUp() {
        alice = creerUtilisateur("Alice");
        bob = creerUtilisateur("Bob");
    }

    @Test
    void creerUneCandidatureCalculeLeScore() throws Exception {
        mockMvc.perform(post("/candidatures").with(user(alice)).with(csrf())
                        .param("entreprise", "Forma Plus")
                        .param("poste", "Formateur web")
                        .param("codePostal", "78400")
                        .param("statut", "A_ENVOYER"))
                .andExpect(status().is3xxRedirection());

        Candidature saved = candidatureRepository.findAll().stream()
                .filter(c -> c.getEntreprise().equals("Forma Plus") && c.getUtilisateur().getId().equals(alice.getId()))
                .findFirst().orElseThrow();
        // 30 (département 78 du profil) + 20 (poste renseigné)
        assertEquals(50, saved.getScore());
        assertEquals("À étudier", saved.getQualification());
    }

    @Test
    void marquerEnvoyeePrevoitUneRelanceDansSeptJours() throws Exception {
        Candidature c = creerCandidature(alice, "Envoi SA");

        mockMvc.perform(post("/candidatures/" + c.getId() + "/envoyee").with(user(alice)).with(csrf()))
                .andExpect(redirectedUrl("/candidatures/" + c.getId()));

        Candidature reloaded = candidatureRepository.findById(c.getId()).orElseThrow();
        assertEquals(StatutCandidature.ENVOYEE, reloaded.getStatut());
        assertEquals(LocalDate.now(java.time.ZoneId.of("Europe/Paris")).plusDays(7), reloaded.getDateRelance());

        // Elle compte dans l'objectif de la semaine du tableau de bord
        mockMvc.perform(get("/tableau-de-bord").with(user(alice)))
                .andExpect(content().string(containsString("1 candidature cette semaine.")));
    }

    @Test
    void unUtilisateurNeVoitPasLesCandidaturesDUnAutre() throws Exception {
        Candidature deAlice = creerCandidature(alice, "Secret d'Alice");

        // Fiche, modification, suppression : 404 pour Bob, comme si elle n'existait pas
        mockMvc.perform(get("/candidatures/" + deAlice.getId()).with(user(bob)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/candidatures/" + deAlice.getId() + "/modifier").with(user(bob)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/candidatures/" + deAlice.getId() + "/supprimer").with(user(bob)).with(csrf()))
                .andExpect(status().isNotFound());

        // Bob ne la voit pas dans sa liste ; Alice, si
        mockMvc.perform(get("/candidatures").with(user(bob)))
                .andExpect(content().string(not(containsString("Secret d&#39;Alice"))));
        mockMvc.perform(get("/candidatures").with(user(alice)))
                .andExpect(content().string(containsString("Secret d&#39;Alice")));

        // Elle existe toujours
        assertEquals(true, candidatureRepository.existsById(deAlice.getId()));
    }

    @Test
    void modifierAvecLIdentifiantDUnAutreEstRefuse() throws Exception {
        Candidature deAlice = creerCandidature(alice, "Alice SARL");

        mockMvc.perform(post("/candidatures").with(user(bob)).with(csrf())
                        .param("id", deAlice.getId().toString())
                        .param("entreprise", "Piratage")
                        .param("statut", "A_ENVOYER"))
                .andExpect(status().isNotFound());

        assertEquals("Alice SARL", candidatureRepository.findById(deAlice.getId()).orElseThrow().getEntreprise());
    }

    @Test
    void lesPagesPrincipalesSAffichent() throws Exception {
        Candidature c = creerCandidature(alice, "Affichage SAS");
        for (String url : new String[]{"/tableau-de-bord", "/candidatures", "/candidatures/nouvelle",
                "/candidatures/" + c.getId(), "/candidatures/" + c.getId() + "/modifier", "/profil",
                "/entreprises"}) {
            mockMvc.perform(get(url).with(user(alice)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void enregistrerLeProfil() throws Exception {
        mockMvc.perform(post("/profil").with(user(bob)).with(csrf())
                        .param("metierVise", "Aide-soignant")
                        .param("motsCles", "maison de retraite")
                        .param("departements", "92 ; 2a"))
                .andExpect(redirectedUrl("/profil"));

        Profil profil = profilRepository.findByUtilisateurId(bob.getId()).orElseThrow();
        assertEquals("92,2A", profil.getDepartements());
        assertEquals(true, profil.isPretPourRecherche());
    }

    @Test
    void unProfilAvecDesDepartementsInvalidesEstRefuse() throws Exception {
        mockMvc.perform(post("/profil").with(user(bob)).with(csrf())
                        .param("departements", "Paris"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("numéros de départements")));
    }

    // ----- Outils -----

    private CandidusUser creerUtilisateur(String prenom) {
        Utilisateur u = new Utilisateur();
        u.setEmail(prenom + "-" + UUID.randomUUID() + "@exemple.fr");
        u.setPrenom(prenom);
        u.setNom("Test");
        u.setMotDePasse("{noop}inutilise");
        u = utilisateurRepository.save(u);

        Profil profil = new Profil();
        profil.setUtilisateur(u);
        profil.setDepartements("78");
        profilRepository.save(profil);

        return new CandidusUser(u.getId(), u.getEmail(), u.getMotDePasse(), u.getPrenom());
    }

    private Candidature creerCandidature(CandidusUser owner, String entreprise) {
        Candidature c = new Candidature();
        c.setUtilisateur(utilisateurRepository.getReferenceById(owner.getId()));
        c.setEntreprise(entreprise);
        return candidatureRepository.save(c);
    }
}
