package fr.candidus.service;

import fr.candidus.entity.Candidature;
import fr.candidus.entity.Profil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CandidatureScoringServiceTest {

    private final CandidatureScoringService service = new CandidatureScoringService();

    @Test
    void candidatureCompleteDansLaZoneVaut100() {
        Candidature c = new Candidature();
        c.setCodePostal("92100");
        c.setTrancheEffectif("12");
        c.setPoste("Formateur");
        c.setOffreUrl("https://exemple.fr/offre");
        c.setSiteWeb("https://exemple.fr");

        assertEquals(100, service.calculerScore(c, profil("78,92")));
        assertEquals("Prioritaire", service.qualification(100));
    }

    @Test
    void candidatureVideHorsZoneVaut0() {
        Candidature c = new Candidature();
        c.setCodePostal("75001");

        assertEquals(0, service.calculerScore(c, profil("78,92")));
        assertEquals("Faible potentiel", service.qualification(0));
    }

    @Test
    void petiteEntreprisePresDeChezSoi() {
        Candidature c = new Candidature();
        c.setCodePostal("78400");
        c.setTrancheEffectif("03");

        assertEquals(40, service.calculerScore(c, profil("78")));
        assertEquals("À étudier", service.qualification(40));
    }

    @Test
    void laCorseEtLOutreMerSontReconnues() {
        assertTrue(CandidatureScoringService.codePostalDuDepartement("20000", "2A"));
        assertTrue(CandidatureScoringService.codePostalDuDepartement("97110", "971"));
        assertFalse(CandidatureScoringService.codePostalDuDepartement("97200", "971"));
    }

    @Test
    void unProfilSansDepartementNeDonneAucunPointDeZone() {
        Candidature c = new Candidature();
        c.setCodePostal("78400");
        assertEquals(0, service.calculerScore(c, profil(null)));
    }

    private static Profil profil(String departements) {
        Profil profil = new Profil();
        profil.setDepartements(departements);
        return profil;
    }
}
