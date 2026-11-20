package fr.candidus.controller;

import fr.candidus.dto.ProfilForm;
import fr.candidus.security.CandidusUser;
import fr.candidus.service.ProfilService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profil")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }

    /** Pourcentage de profil complété, affiché en haut de la page (aussi après une erreur de saisie). */
    @ModelAttribute("completion")
    public int completion(@AuthenticationPrincipal CandidusUser user) {
        return profilService.getOrCreate(user.getId()).getCompletion();
    }

    @GetMapping
    public String showForm(@AuthenticationPrincipal CandidusUser user, Model model) {
        model.addAttribute("profil", ProfilForm.fromEntity(profilService.getOrCreate(user.getId())));
        return "profil";
    }

    @PostMapping
    public String save(@AuthenticationPrincipal CandidusUser user,
                       @Valid @ModelAttribute("profil") ProfilForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profil";
        }
        profilService.update(user.getId(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Profil enregistré.");
        return "redirect:/profil";
    }
}
