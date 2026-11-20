package fr.candidus.controller;

import fr.candidus.security.CandidusUser;
import fr.candidus.service.DashboardService;
import fr.candidus.service.ProfilService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final ProfilService profilService;

    public DashboardController(DashboardService dashboardService, ProfilService profilService) {
        this.dashboardService = dashboardService;
        this.profilService = profilService;
    }

    @GetMapping("/tableau-de-bord")
    public String dashboard(@AuthenticationPrincipal CandidusUser user, Model model) {
        model.addAttribute("prenom", user.getPrenom());
        model.addAttribute("stats", dashboardService.compute(user.getId()));
        model.addAttribute("profilPret", profilService.getOrCreate(user.getId()).isPretPourRecherche());
        return "dashboard";
    }
}
