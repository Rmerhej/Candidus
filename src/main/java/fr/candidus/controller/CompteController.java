package fr.candidus.controller;

import fr.candidus.exception.CompteException;
import fr.candidus.security.CandidusUser;
import fr.candidus.service.CompteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Page « Mon compte » : mot de passe et suppression du compte. */
@Controller
@RequestMapping("/compte")
public class CompteController {

    private final CompteService compteService;

    public CompteController(CompteService compteService) {
        this.compteService = compteService;
    }

    @GetMapping
    public String afficher(@AuthenticationPrincipal CandidusUser user, Model model) {
        model.addAttribute("email", user.getUsername());
        return "compte";
    }

    @PostMapping("/mot-de-passe")
    public String changerMotDePasse(@AuthenticationPrincipal CandidusUser user,
                                    @RequestParam(required = false) String actuel,
                                    @RequestParam(required = false) String nouveau,
                                    @RequestParam(required = false) String confirmation,
                                    RedirectAttributes redirect) {
        try {
            compteService.changerMotDePasse(user.getId(), actuel, nouveau, confirmation);
            redirect.addFlashAttribute("successMessage", "Votre mot de passe a été modifié.");
        } catch (CompteException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/compte";
    }

    @PostMapping("/supprimer")
    public String supprimer(@AuthenticationPrincipal CandidusUser user,
                            Authentication authentication,
                            @RequestParam(required = false) String motDePasse,
                            @RequestParam(required = false) String confirmation,
                            HttpServletRequest request, HttpServletResponse response,
                            RedirectAttributes redirect) {
        if (confirmation == null || confirmation.isBlank()) {
            redirect.addFlashAttribute("errorMessage", "Cochez la case pour confirmer la suppression.");
            return "redirect:/compte";
        }
        try {
            compteService.supprimerCompte(user.getId(), motDePasse);
        } catch (CompteException e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/compte";
        }
        // Le compte n'existe plus : on ferme la session.
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?supprime";
    }
}
