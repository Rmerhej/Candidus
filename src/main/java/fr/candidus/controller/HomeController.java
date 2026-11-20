package fr.candidus.controller;

import fr.candidus.security.CandidusUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** Page de présentation pour les visiteurs ; un utilisateur connecté va directement à son tableau de bord. */
    @GetMapping("/")
    public String home(@AuthenticationPrincipal CandidusUser user) {
        return user == null ? "index" : "redirect:/tableau-de-bord";
    }
}
