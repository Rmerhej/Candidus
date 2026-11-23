package fr.candidus.repository;

import fr.candidus.entity.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfilRepository extends JpaRepository<Profil, Long> {

    Optional<Profil> findByUtilisateurId(Long utilisateurId);

    /** Supprime le profil d'un utilisateur (suppression de son compte). */
    @Modifying(clearAutomatically = true)
    @Query("delete from Profil p where p.utilisateur.id = :utilisateurId")
    void deleteByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}
