package fr.candidus.controller;

import fr.candidus.client.ApiUnavailableException;
import fr.candidus.dto.ImportReport;
import fr.candidus.entity.Profil;
import fr.candidus.security.CandidusUser;
import fr.candidus.service.CompanyImportService;
import fr.candidus.service.ProfilService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Recherche d'entreprises près de chez soi, selon les mots-clés et départements du profil.
 */
@Controller
@RequestMapping("/entreprises")
public class ImportController {

    private final CompanyImportService importService;
    private final ProfilService profilService;

    public ImportController(CompanyImportService importService, ProfilService profilService) {
        this.importService = importService;
        this.profilService = profilService;
    }

    @GetMapping
    public String search(@AuthenticationPrincipal CandidusUser user,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {
        Profil profil = profilService.getOrCreate(user.getId());
        model.addAttribute("profil", profil);

        if (profil.isPretPourRecherche()) {
            try {
                model.addAttribute("result", importService.search(user.getId(), profil, page));
            } catch (ApiUnavailableException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return "entreprises/search";
    }

    /** Ajoute les entreprises cochées aux candidatures, puis revient sur la même page de résultats. */
    @PostMapping
    public String importSelected(@AuthenticationPrincipal CandidusUser user,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(required = false) List<String> sirens,
                                 RedirectAttributes redirectAttributes) {
        Profil profil = profilService.getOrCreate(user.getId());
        if (!profil.isPretPourRecherche()) {
            return "redirect:/entreprises";
        }

        try {
            ImportReport report = importService.importCompanies(user.getId(), profil, page, sirens);
            if (report.imported() == 0 && report.alreadyPresent() == 0 && report.notFound() == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cochez au moins une entreprise.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", report.toMessage());
            }
        } catch (ApiUnavailableException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/entreprises?page=" + Math.max(page, 1);
    }
}
