package fr.candidus.dto;

import fr.candidus.entity.Profil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardStatsTest {

    private static DashboardStats semaine(long envoyees) {
        return new DashboardStats(0, 0, 0, 0, null, List.of(), List.of(), envoyees, 5);
    }

    @Test
    void lesMessagesDEncouragementSuiventLaSemaine() {
        assertEquals("Une première candidature cette semaine ?", semaine(0).getEncouragement());
        assertEquals("Plus que 2, continuez comme ça !", semaine(3).getEncouragement());
        assertEquals("Plus qu'une, vous y êtes presque !", semaine(4).getEncouragement());
        assertEquals("Objectif atteint, bravo !", semaine(7).getEncouragement());
    }

    @Test
    void uneCaseParCandidatureDeLObjectif() {
        assertEquals(List.of(true, true, false, false, false), semaine(2).getPasSemaine());
    }

    @Test
    void completionDuProfil() {
        Profil profil = new Profil();
        assertEquals(0, profil.getCompletion());
        profil.setMetierVise("Formateur");
        profil.setMotsCles("organisme de formation");
        profil.setDepartements("78");
        profil.setPointsForts(" ");
        // 3 champs remplis sur 7
        assertEquals(43, profil.getCompletion());
    }
}
