package fr.candidus.exception;

/**
 * Candidature inexistante, ou appartenant à un autre utilisateur : dans les deux cas,
 * on répond "introuvable" (404) pour ne rien révéler.
 */
public class CandidatureNotFoundException extends RuntimeException {

    public CandidatureNotFoundException(Long id) {
        super("La candidature n°" + id + " est introuvable.");
    }
}
