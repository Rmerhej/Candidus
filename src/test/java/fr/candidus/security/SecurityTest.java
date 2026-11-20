package fr.candidus.security;

import fr.candidus.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Inscription, connexion et pages protégées, de bout en bout (vraie configuration, base H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void laPageDAccueilEstPublique() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Créer mon compte")));
    }

    @Test
    void lesPagesDInformationSontPubliquesEtOntLePiedDePage() throws Exception {
        for (String url : new String[]{"/aide", "/mentions-legales", "/conditions", "/confidentialite"}) {
            mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Questions fréquentes")));
        }
    }

    @Test
    void unePageProtegeeRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/candidatures"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void inscriptionPuisConnexion() throws Exception {
        inscrire("Claire.Martin@Exemple.fr")
                .andExpect(redirectedUrl("/login?inscrit"));

        // L'e-mail est enregistré en minuscules, le mot de passe est haché
        var utilisateur = utilisateurRepository.findByEmail("claire.martin@exemple.fr").orElseThrow();
        assertTrue(utilisateur.getMotDePasse().startsWith("{bcrypt}"));

        mockMvc.perform(formLogin().userParameter("email").user("claire.martin@exemple.fr")
                        .password("motdepasse-solide"))
                .andExpect(authenticated().withUsername("claire.martin@exemple.fr"))
                .andExpect(redirectedUrl("/tableau-de-bord"));
    }

    @Test
    void unEmailNePeutPasServirDeuxFois() throws Exception {
        inscrire("doublon@exemple.fr").andExpect(redirectedUrl("/login?inscrit"));

        inscrire("DOUBLON@exemple.fr")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Un compte existe déjà")));
    }

    @Test
    void lesMotsDePasseDoiventCorrespondre() throws Exception {
        mockMvc.perform(post("/inscription").with(csrf())
                        .param("prenom", "Paul")
                        .param("nom", "Durand")
                        .param("email", "paul@exemple.fr")
                        .param("motDePasse", "motdepasse-solide")
                        .param("confirmation", "autre-motdepasse")
                        .param("accepteConditions", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ne correspondent pas")));
    }

    @Test
    void connexionRefuseeAvecUnMauvaisMotDePasse() throws Exception {
        inscrire("refus@exemple.fr");
        mockMvc.perform(formLogin().userParameter("email").user("refus@exemple.fr").password("mauvais-mdp"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void unFormulaireSansJetonCsrfEstRefuse() throws Exception {
        mockMvc.perform(post("/inscription")
                        .param("email", "sans-csrf@exemple.fr"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deconnexion() throws Exception {
        mockMvc.perform(logout())
                .andExpect(redirectedUrl("/login?logout"));
    }

    private org.springframework.test.web.servlet.ResultActions inscrire(String email) throws Exception {
        return mockMvc.perform(post("/inscription").with(csrf())
                .param("prenom", "Claire")
                .param("nom", "Martin")
                .param("email", email)
                .param("motDePasse", "motdepasse-solide")
                .param("confirmation", "motdepasse-solide")
                .param("accepteConditions", "true"));
    }
}
