package fr.candidus.client;

import fr.candidus.client.dto.ApiCompany;
import fr.candidus.client.dto.ApiSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Aucun appel réseau : MockRestServiceServer simule l'API et vérifie la requête envoyée.
 */
class RechercheEntreprisesClientTest {

    private static final String BASE_URL = "https://api.test";

    // Extrait réduit d'une réponse de l'API
    private static final String JSON = """
            {
              "results": [
                {
                  "siren": "123456789",
                  "nom_complet": "FORMA PLUS",
                  "activite_principale": "85.59A",
                  "tranche_effectif_salarie": "11",
                  "categorie_entreprise": "PME",
                  "siege": {
                    "adresse": "1 RUE DE PARIS 78400 CHATOU",
                    "code_postal": "78400",
                    "libelle_commune": "CHATOU"
                  },
                  "champ_inconnu": "ignoré sans erreur"
                }
              ],
              "total_results": 1,
              "page": 1,
              "per_page": 25,
              "total_pages": 1
            }
            """;

    private MockRestServiceServer server;
    private RechercheEntreprisesClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RechercheEntreprisesClient(builder.build());
    }

    @Test
    void rechercheParMotsClesEtDepartements() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryContains("q=organisme de formation"))
                .andExpect(queryContains("departement=78,92"))
                .andExpect(queryContains("tranche_effectif_salarie=" + RechercheEntreprisesClient.AVEC_SALARIES))
                .andExpect(queryContains("etat_administratif=A"))
                .andExpect(queryContains("per_page=25"))
                .andExpect(queryContains("page=2"))
                .andRespond(withSuccess(JSON, MediaType.APPLICATION_JSON));

        ApiSearchResponse response = client.search("organisme de formation", List.of("78", "92"), 2);

        server.verify();
        assertEquals(1, response.totalResults());

        ApiCompany company = response.safeResults().getFirst();
        assertEquals("123456789", company.siren());
        assertEquals("FORMA PLUS", company.nomComplet());
        assertEquals("11", company.trancheEffectif());
        assertEquals("CHATOU", company.siege().libelleCommune());
        assertEquals("78400", company.siege().codePostal());
    }

    @Test
    void erreurDeLApiDevientApiUnavailableException() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andRespond(withServerError());

        assertThrows(ApiUnavailableException.class,
                () -> client.search("boulangerie", List.of("78"), 1));
    }

    /** Vérifie un paramètre dans l'URL, quel que soit l'encodage (%2C, %20, +). */
    private static RequestMatcher queryContains(String expected) {
        return request -> {
            String query = URLDecoder.decode(request.getURI().getRawQuery(), StandardCharsets.UTF_8);
            List<String> params = List.of(query.split("&"));
            assertTrue(params.contains(expected),
                    () -> "Paramètre attendu : " + expected + " — reçu : " + query);
        };
    }
}
