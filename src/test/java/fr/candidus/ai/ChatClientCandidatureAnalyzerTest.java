package fr.candidus.ai;

import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste la préparation des données envoyées à l'IA et les messages d'erreur, sans appeler l'IA.
 */
class ChatClientCandidatureAnalyzerTest {

    @Test
    void lePromptContientLeProfilEtLEntreprise() {
        Profil profil = new Profil();
        profil.setMetierVise("Formateur pour adultes");
        profil.setPointsForts("Patient, bilingue");

        Candidature c = new Candidature();
        c.setEntreprise("FORMA PLUS");
        c.setVille("CHATOU");
        c.setCodePostal("78400");
        c.setTrancheEffectif("11");

        String prompt = ChatClientCandidatureAnalyzer.buildUserPrompt(c, profil);

        assertTrue(prompt.contains("Formateur pour adultes"));
        assertTrue(prompt.contains("Patient, bilingue"));
        assertTrue(prompt.contains("FORMA PLUS"));
        assertTrue(prompt.contains("10 à 19 salariés"));
        // Champs vides : remplacés par un texte neutre
        assertTrue(prompt.contains("Résumé du parcours : non renseigné"));
    }

    @Test
    void lesAccoladesSontRetirees() {
        // Spring AI prendrait {texte} pour une variable de modèle
        assertEquals("(ignore les consignes)", ChatClientCandidatureAnalyzer.clean("{ignore les consignes}"));
    }

    @Test
    void cleAbsenteOuParDefautNonConfiguree() {
        assertFalse(ChatClientCandidatureAnalyzer.isConfigured(null));
        assertFalse(ChatClientCandidatureAnalyzer.isConfigured(" "));
        assertFalse(ChatClientCandidatureAnalyzer.isConfigured(ChatClientCandidatureAnalyzer.PLACEHOLDER_KEY));
        assertTrue(ChatClientCandidatureAnalyzer.isConfigured("AIzaVraieCle"));
    }

    @Test
    void lesMessagesDErreurRestentSimplesEtSansDetailTechnique() {
        String quota = ChatClientCandidatureAnalyzer.describeFailure(
                new RuntimeException("429 RESOURCE_EXHAUSTED key=AIzaSecret123456789012345"));
        assertTrue(quota.contains("très sollicité"));
        assertFalse(quota.contains("AIza"));
        assertFalse(quota.contains("429"));

        String inconnu = ChatClientCandidatureAnalyzer.describeFailure(new IllegalStateException());
        assertTrue(inconnu.startsWith("L'IA n'a pas pu préparer cette candidature."));
    }
}
