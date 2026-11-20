package fr.candidus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pages d'information publiques, accessibles depuis le pied de page.
 */
@Controller
public class PagesController {

    @GetMapping("/aide")
    public String aide() {
        return "pages/aide";
    }

    @GetMapping("/mentions-legales")
    public String mentionsLegales() {
        return "pages/mentions-legales";
    }

    @GetMapping("/conditions")
    public String conditions() {
        return "pages/conditions";
    }

    @GetMapping("/confidentialite")
    public String confidentialite() {
        return "pages/confidentialite";
    }
}
