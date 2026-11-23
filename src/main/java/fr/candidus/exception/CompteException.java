package fr.candidus.exception;

/**
 * Une demande sur le compte (changer le mot de passe, supprimer le compte) est refusée.
 * Le message est écrit pour être montré tel quel à l'utilisateur.
 */
public class CompteException extends RuntimeException {

    public CompteException(String message) {
        super(message);
    }
}
