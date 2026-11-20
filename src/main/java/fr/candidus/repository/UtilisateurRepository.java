package fr.candidus.repository;

import fr.candidus.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /** Les e-mails sont enregistrés en minuscules : appeler avec une adresse déjà en minuscules. */
    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
