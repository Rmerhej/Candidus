package fr.candidus.exception;

/**
 * Un compte existe déjà avec cette adresse e-mail.
 */
public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException() {
        super("Un compte existe déjà avec cette adresse e-mail.");
    }
}
