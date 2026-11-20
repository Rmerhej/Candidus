package fr.candidus.controller;

import fr.candidus.dto.InscriptionForm;
import fr.candidus.exception.EmailDejaUtiliseException;
import fr.candidus.security.CandidusUser;
import fr.candidus.service.InscriptionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inscription")
public class InscriptionController {

    private final InscriptionService inscriptionService;

    public InscriptionController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    @GetMapping
    public String showForm(@AuthenticationPrincipal CandidusUser user, Model model) {
        if (user != null) {
            return "redirect:/tableau-de-bord";
        }
        model.addAttribute("form", new InscriptionForm());
        return "inscription";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("form") InscriptionForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "inscription";
        }
        try {
            inscriptionService.inscrire(form);
        } catch (EmailDejaUtiliseException e) {
            result.rejectValue("email", "email.deja.utilise", e.getMessage());
            return "inscription";
        }
        return "redirect:/login?inscrit";
    }
}
