package fr.candidus.controller;

import fr.candidus.ai.AiUnavailableException;
import fr.candidus.dto.LettreForm;
import fr.candidus.entity.Candidature;
import fr.candidus.enums.TrancheEffectif;
import fr.candidus.security.CandidusUser;
import fr.candidus.service.CandidatureAnalysisService;
import fr.candidus.service.CandidatureService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Fiche d'une candidature : informations, suivi, analyse IA et lettre.
 */
@Controller
@RequestMapping("/candidatures/{id}")
public class CandidatureDetailController {

    private final CandidatureService candidatureService;
    private final CandidatureAnalysisService analysisService;

    public CandidatureDetailController(CandidatureService candidatureService,
                                       CandidatureAnalysisService analysisService) {
        this.candidatureService = candidatureService;
        this.analysisService = analysisService;
    }

    @GetMapping
    public String show(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id, Model model) {
        Candidature candidature = candidatureService.find(user.getId(), id);
        fillModel(model, candidature);

        // Formulaire de la lettre, pré-rempli (sauf si on revient d'une erreur de validation)
        if (!model.containsAttribute("lettre")) {
            LettreForm lettre = new LettreForm();
            lettre.setSubject(candidature.getLettreObjet());
            lettre.setBody(candidature.getLettreCorps());
            model.addAttribute("lettre", lettre);
        }
        return "candidatures/detail";
    }

    @PostMapping("/preparer")
    public String prepare(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            analysisService.preparer(user.getId(), id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Lettre préparée. Relisez-la et personnalisez-la avant de l'envoyer.");
        } catch (AiUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/candidatures/" + id;
    }

    @PostMapping("/lettre")
    public String saveLetter(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id,
                             @Valid @ModelAttribute("lettre") LettreForm lettre,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // On réaffiche la fiche avec les erreurs, sans perdre le texte saisi
            fillModel(model, candidatureService.find(user.getId(), id));
            return "candidatures/detail";
        }
        analysisService.enregistrerLettre(user.getId(), id, lettre.getSubject(), lettre.getBody());
        redirectAttributes.addFlashAttribute("successMessage", "Lettre enregistrée.");
        return "redirect:/candidatures/" + id;
    }

    private static void fillModel(Model model, Candidature candidature) {
        model.addAttribute("candidature", candidature);
        model.addAttribute("trancheLibelle", TrancheEffectif.libelle(candidature.getTrancheEffectif()));
    }
}
