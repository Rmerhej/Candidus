package fr.candidus.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarTest {

    @Test
    void initialeIgnoreLaPonctuation() {
        assertEquals("F", Avatar.initiale("forma plus"));
        assertEquals("É", Avatar.initiale("« École du Val »"));
        assertEquals("?", Avatar.initiale("  "));
        assertEquals("?", Avatar.initiale(null));
    }

    @Test
    void uneEntrepriseGardeToujoursLaMemeTeinte() {
        int teinte = Avatar.teinte("Forma Plus");
        assertEquals(teinte, Avatar.teinte("FORMA PLUS"));
        assertTrue(teinte >= 0 && teinte <= 3);
    }
}
