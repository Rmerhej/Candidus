package fr.candidus.util;

import java.util.Locale;

/**
 * Pastille d'une entreprise : son initiale et une teinte (0 à 3) toujours la même pour un même nom.
 */
public final class Avatar {

    private Avatar() {
    }

    /** Première lettre ou chiffre du nom, en majuscule ("?" si le nom est vide). */
    public static String initiale(String nom) {
        if (nom == null) {
            return "?";
        }
        for (int i = 0; i < nom.length(); i++) {
            char c = nom.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                return String.valueOf(c).toUpperCase(Locale.ROOT);
            }
        }
        return "?";
    }

    /** Teinte de 0 à 3, calculée à partir du nom : une entreprise garde toujours la même couleur. */
    public static int teinte(String nom) {
        return nom == null ? 0 : Math.floorMod(nom.toLowerCase(Locale.ROOT).hashCode(), 4);
    }
}
