package fr.candidus.web;

import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import fr.candidus.entity.Utilisateur;
import fr.candidus.repository.CandidatureRepository;
import fr.candidus.repository.ProfilRepository;
import fr.candidus.repository.UtilisateurRepository;
import fr.candidus.security.CandidusUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * « Mon compte » : changer son mot de passe et supprimer son compte (avec ses données, et seulement les siennes).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CompteWebTest {

    private static final String MOT_DE_PASSE = "mot-de-passe-solide";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ProfilRepository profilRepository;
    @Autowired
    private CandidatureRepository candidatureRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private CandidusUser alice;
    private CandidusUser bob;

    @BeforeEach
    void setUp() {
        alice = creerUtilisateur("Alice");
        bob = creerUtilisateur("Bob");
    }

    @Test
    void laPageEstProtegee() throws Exception {
        mockMvc.perform(get("/compte"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void laPageAfficheLAdresseSansLeMotDePasseHache() throws Exception {
        mockMvc.perform(get("/compte").with(user(alice)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(alice.getUsername())))
                .andExpect(content().string(containsString("Supprimer définitivement mon compte")))
                .andExpect(content().string(not(containsString(alice.getPassword()))));
    }

    @Test
    void leMotDePasseChangeQuandLAncienEstBon() throws Exception {
        mockMvc.perform(post("/compte/mot-de-passe").with(user(alice)).with(csrf())
                        .param("actuel", MOT_DE_PASSE)
                        .param("nouveau", "nouveau-mot-de-passe")
                        .param("confirmation", "nouveau-mot-de-passe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));

        String hache = utilisateurRepository.findById(alice.getId()).orElseThrow().getMotDePasse();
        assertTrue(passwordEncoder.matches("nouveau-mot-de-passe", hache));
        assertFalse(passwordEncoder.matches(MOT_DE_PASSE, hache));
    }

    @Test
    void leMotDePasseResteSiLAncienEstFauxOuLeNouveauInvalide() throws Exception {
        String avant = utilisateurRepository.findById(alice.getId()).orElseThrow().getMotDePasse();

        mockMvc.perform(post("/compte/mot-de-passe").with(user(alice)).with(csrf())
                        .param("actuel", "pas-le-bon-mot-de-passe")
                        .param("nouveau", "nouveau-mot-de-passe")
                        .param("confirmation", "nouveau-mot-de-passe"))
                .andExpect(flash().attributeExists("errorMessage"));
        mockMvc.perform(post("/compte/mot-de-passe").with(user(alice)).with(csrf())
                        .param("actuel", MOT_DE_PASSE)
                        .param("nouveau", "court")
                        .param("confirmation", "court"))
                .andExpect(flash().attributeExists("errorMessage"));
        mockMvc.perform(post("/compte/mot-de-passe").with(user(alice)).with(csrf())
                        .param("actuel", MOT_DE_PASSE)
                        .param("nouveau", "nouveau-mot-de-passe")
                        .param("confirmation", "un-autre-mot-de-passe"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertTrue(avant.equals(utilisateurRepository.findById(alice.getId()).orElseThrow().getMotDePasse()));
    }

    @Test
    void lesDonneesDeLUtilisateurSontSupprimeesMaisPasCellesDesAutres() throws Exception {
        creerCandidature(alice, "Entreprise d'Alice");
        creerCandidature(bob, "Entreprise de Bob");

        mockMvc.perform(post("/compte/supprimer").with(user(alice)).with(csrf())
                        .param("motDePasse", MOT_DE_PASSE).param("confirmation", "oui"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?supprime"));

        assertTrue(utilisateurRepository.findById(alice.getId()).isEmpty());
        assertTrue(profilRepository.findByUtilisateurId(alice.getId()).isEmpty());
        assertTrue(candidatureRepository.findAll().stream()
                .noneMatch(c -> c.getUtilisateur().getId().equals(alice.getId())));

        // Bob n'est pas touché
        assertTrue(utilisateurRepository.findById(bob.getId()).isPresent());
        assertTrue(profilRepository.findByUtilisateurId(bob.getId()).isPresent());
        assertTrue(candidatureRepository.findAll().stream()
                .anyMatch(c -> c.getUtilisateur().getId().equals(bob.getId())));
    }

    @Test
    void leCompteResteSansLaCaseOuAvecUnMauvaisMotDePasse() throws Exception {
        mockMvc.perform(post("/compte/supprimer").with(user(alice)).with(csrf())
                        .param("motDePasse", MOT_DE_PASSE))
                .andExpect(flash().attributeExists("errorMessage"));
        mockMvc.perform(post("/compte/supprimer").with(user(alice)).with(csrf())
                        .param("motDePasse", "pas-le-bon-mot-de-passe").param("confirmation", "oui"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertTrue(utilisateurRepository.findById(alice.getId()).isPresent());
    }

    private CandidusUser creerUtilisateur(String prenom) {
        Utilisateur u = new Utilisateur();
        u.setEmail(prenom + "-" + UUID.randomUUID() + "@exemple.fr");
        u.setPrenom(prenom);
        u.setNom("Test");
        u.setMotDePasse(passwordEncoder.encode(MOT_DE_PASSE));
        u = utilisateurRepository.save(u);

        Profil profil = new Profil();
        profil.setUtilisateur(u);
        profil.setDepartements("78");
        profilRepository.save(profil);

        return new CandidusUser(u.getId(), u.getEmail(), u.getMotDePasse(), u.getPrenom());
    }

    private void creerCandidature(CandidusUser owner, String entreprise) {
        Candidature c = new Candidature();
        c.setUtilisateur(utilisateurRepository.getReferenceById(owner.getId()));
        c.setEntreprise(entreprise);
        candidatureRepository.save(c);
    }
}
