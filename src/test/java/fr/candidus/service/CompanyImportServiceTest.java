package fr.candidus.service;

import fr.candidus.client.RechercheEntreprisesClient;
import fr.candidus.client.dto.ApiSearchResponse;
import fr.candidus.entity.Profil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CompanyImportServiceTest {

    private final RechercheEntreprisesClient client = mock(RechercheEntreprisesClient.class);
    private final CompanyImportService service = new CompanyImportService(client, null, null, new CandidatureScoringService());

    @Test
    void unePhraseSansResultatEstReduiteASonMotLePlusParlant() {
        when(client.search(eq("organisme de formation"), any(), anyInt()))
                .thenReturn(new ApiSearchResponse(List.of(), 0, 1, 25, 0));

        assertEquals("formation", service.requeteEffective(profil("organisme de formation")));
    }

    @Test
    void unePhraseQuiDonneDesResultatsEstGardee() {
        when(client.search(eq("maison de retraite"), any(), anyInt()))
                .thenReturn(new ApiSearchResponse(List.of(), 12, 1, 25, 1));

        assertEquals("maison de retraite", service.requeteEffective(profil("maison de retraite")));
    }

    @Test
    void unSeulMotEstUtiliseTelQuelSansAppelEnPlus() {
        assertEquals("boulangerie", service.requeteEffective(profil("boulangerie")));
        verifyNoInteractions(client);
    }

    private static Profil profil(String motsCles) {
        Profil profil = new Profil();
        profil.setMotsCles(motsCles);
        profil.setDepartements("78");
        return profil;
    }
}
