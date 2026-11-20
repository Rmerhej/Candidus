package fr.candidus.client;

import fr.candidus.client.dto.ApiSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

/**
 * Client HTTP de l'API publique Recherche d'entreprises (gratuite, sans clé).
 * Documentation : https://recherche-entreprises.api.gouv.fr/docs/
 */
@Component
public class RechercheEntreprisesClient {

    /** Maximum autorisé par l'API. */
    public static final int PER_PAGE = 25;

    /** L'API ne sert pas plus de 10 000 résultats par recherche, soit 400 pages de 25. */
    public static final int MAX_PAGE = 400;

    private final RestClient restClient;

    @Autowired
    public RechercheEntreprisesClient(
            @Value("${candidus.api.recherche-entreprises.base-url:https://recherche-entreprises.api.gouv.fr}")
            String baseUrl) {

        // Délais maximum : sans eux, une API lente bloquerait la page indéfiniment
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Candidus (https://candidus.app)")
                .build();
    }

    /** Constructeur utilisé par les tests, avec un RestClient simulé. */
    RechercheEntreprisesClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Tranches INSEE "au moins 1 salarié" : on écarte les entreprises sans salarié,
     * qui ne recrutent quasiment jamais (codes 00 et NN).
     */
    static final String AVEC_SALARIES = "01,02,03,11,12,21,22,31,32,41,42,51,52,53";

    /**
     * Recherche les entreprises actives qui emploient des salariés, par mots-clés et départements.
     *
     * @param motsCles     texte libre, ex : "organisme de formation" (nom, activité, enseigne)
     * @param departements codes de départements, ex : ["78", "92"]
     * @param page         numéro de page, à partir de 1 (convention de l'API)
     */
    public ApiSearchResponse search(String motsCles, List<String> departements, int page) {

        String codesDepartements = String.join(",", departements);

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", motsCles)
                            .queryParam("departement", codesDepartements)
                            .queryParam("tranche_effectif_salarie", AVEC_SALARIES)
                            .queryParam("etat_administratif", "A")
                            // Réponse allégée + adresse du siège (sans les dirigeants)
                            .queryParam("minimal", "true")
                            .queryParam("include", "siege")
                            .queryParam("per_page", PER_PAGE)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .body(ApiSearchResponse.class);

        } catch (RestClientException e) {
            throw new ApiUnavailableException(
                    "L'API Recherche d'entreprises ne répond pas pour le moment. Réessayez dans quelques instants.", e);
        }
    }
}
