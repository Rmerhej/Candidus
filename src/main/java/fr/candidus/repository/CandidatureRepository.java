package fr.candidus.repository;

import fr.candidus.entity.Candidature;
import fr.candidus.enums.StatutCandidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Toutes les requêtes filtrent par utilisateur : un utilisateur ne voit jamais les candidatures d'un autre.
 */
public interface CandidatureRepository
        extends JpaRepository<Candidature, Long>,
        JpaSpecificationExecutor<Candidature> {

    /** Une candidature de cet utilisateur, ou vide si elle n'existe pas ou appartient à quelqu'un d'autre. */
    Optional<Candidature> findByIdAndUtilisateurId(Long id, Long utilisateurId);

    /** Parmi les SIREN donnés, ceux que cet utilisateur a déjà importés (une seule requête SQL). */
    @Query("select c.siren from Candidature c where c.utilisateur.id = :utilisateurId and c.siren in :sirens")
    Set<String> findExistingSirens(@Param("utilisateurId") Long utilisateurId,
                                   @Param("sirens") Collection<String> sirens);

    /** Une ligne [statut, nombre] par statut. */
    @Query("select c.statut, count(c) from Candidature c where c.utilisateur.id = :utilisateurId group by c.statut")
    List<Object[]> countByStatut(@Param("utilisateurId") Long utilisateurId);

    /** Relances à faire : date atteinte et toujours sans réponse. */
    @Query("""
            select c from Candidature c
            where c.utilisateur.id = :utilisateurId
              and c.statut in :statuts
              and c.dateRelance <= :aujourdhui
            order by c.dateRelance asc
            """)
    List<Candidature> findRelancesDues(@Param("utilisateurId") Long utilisateurId,
                                       @Param("statuts") Collection<StatutCandidature> statuts,
                                       @Param("aujourdhui") LocalDate aujourdhui);
}
