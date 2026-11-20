package fr.candidus.controller;

import fr.candidus.dto.CandidatureForm;
import fr.candidus.entity.Candidature;
import fr.candidus.enums.StatutCandidature;
import fr.candidus.security.CandidusUser;
import fr.candidus.service.CandidatureService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Liste, création, modification et suppression des candidatures de l'utilisateur connecté.
 */
@Controller
@RequestMapping("/candidatures")
public class CandidatureController {

    private final CandidatureService candidatureService;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    @ModelAttribute("statuts")
    public StatutCandidature[] statuts() {
        return StatutCandidature.values();
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CandidusUser user,
                       @RequestParam(required = false) StatutCandidature statut,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        Page<Candidature> candidatures = candidatureService.search(user.getId(), statut, q, page, size);

        model.addAttribute("candidatures", candidatures);
        model.addAttribute("statut", statut);
        model.addAttribute("q", q);
        model.addAttribute("currentPage", candidatures.getNumber());
        model.addAttribute("pageSize", candidatures.getSize());
        model.addAttribute("totalPages", candidatures.getTotalPages());
        model.addAttribute("hasFilters", statut != null || (q != null && !q.isBlank()));
        return "candidatures/list";
    }

    @GetMapping("/nouvelle")
    public String newForm(Model model) {
        model.addAttribute("candidature", new CandidatureForm());
        return "candidatures/form";
    }

    @GetMapping("/{id}/modifier")
    public String editForm(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id, Model model) {
        model.addAttribute("candidature", CandidatureForm.fromEntity(candidatureService.find(user.getId(), id)));
        return "candidatures/form";
    }

    @PostMapping
    public String save(@AuthenticationPrincipal CandidusUser user,
                       @Valid @ModelAttribute("candidature") CandidatureForm form,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "candidatures/form";
        }
        boolean isNew = form.getId() == null;
        Candidature saved = candidatureService.save(user.getId(), form);
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Candidature ajoutée." : "Candidature modifiée.");
        return "redirect:/candidatures/" + saved.getId();
    }

    @PostMapping("/{id}/supprimer")
    public String delete(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        candidatureService.delete(user.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Candidature supprimée.");
        return "redirect:/candidatures";
    }

    @PostMapping("/{id}/envoyee")
    public String markSent(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        candidatureService.marquerEnvoyee(user.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Bravo ! Candidature notée comme envoyée. Je vous rappellerai de relancer dans "
                        + CandidatureService.JOURS_AVANT_RELANCE + " jours.");
        return "redirect:/candidatures/" + id;
    }

    @PostMapping("/{id}/relancee")
    public String markFollowedUp(@AuthenticationPrincipal CandidusUser user, @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        candidatureService.marquerRelancee(user.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Relance notée. Prochain rappel dans "
                + CandidatureService.JOURS_AVANT_RELANCE + " jours.");
        return "redirect:/candidatures/" + id;
    }
}
