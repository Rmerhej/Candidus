package fr.candidus.service;

import fr.candidus.dto.DashboardStats;
import fr.candidus.dto.DashboardStats.CountItem;
import fr.candidus.enums.StatutCandidature;
import fr.candidus.repository.CandidatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calcule les chiffres du tableau de bord de l'utilisateur connecté.
 */
@Service
public class DashboardService {

    private final CandidatureRepository candidatureRepository;
    private final Clock clock;

    public DashboardService(CandidatureRepository candidatureRepository, Clock clock) {
        this.candidatureRepository = candidatureRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardStats compute(Long utilisateurId) {
        Map<StatutCandidature, Long> counts = new EnumMap<>(StatutCandidature.class);
        for (Object[] row : candidatureRepository.countByStatut(utilisateurId)) {
            if (row[0] instanceof StatutCandidature statut) {
                counts.put(statut, ((Number) row[1]).longValue());
            }
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long envoyees = sum(counts, StatutCandidature.ENVOYEES);
        long reponses = sum(counts, StatutCandidature.REPONSES);
        long entretiens = counts.getOrDefault(StatutCandidature.ENTRETIEN, 0L)
                + counts.getOrDefault(StatutCandidature.EMBAUCHE, 0L);

        // Tous les statuts, dans l'ordre du suivi, même ceux à 0
        List<CountItem> parStatut = new ArrayList<>();
        for (StatutCandidature statut : StatutCandidature.values()) {
            long nombre = counts.getOrDefault(statut, 0L);
            int pourcentage = total == 0 ? 0 : (int) Math.round(nombre * 100.0 / total);
            parStatut.add(new CountItem(statut.getLibelle(), nombre, pourcentage, statut.getCouleur()));
        }

        return new DashboardStats(
                total,
                envoyees,
                reponses,
                entretiens,
                percentOrNull(reponses, envoyees),
                parStatut,
                candidatureRepository.findRelancesDues(
                        utilisateurId, StatutCandidature.EN_ATTENTE, LocalDate.now(clock)));
    }

    private static long sum(Map<StatutCandidature, Long> counts, Set<StatutCandidature> statuts) {
        return statuts.stream().mapToLong(s -> counts.getOrDefault(s, 0L)).sum();
    }

    /** Pourcentage arrondi, ou null si le dénominateur est 0 (affiché "—"). */
    static Integer percentOrNull(long part, long whole) {
        return whole == 0 ? null : (int) Math.round(part * 100.0 / whole);
    }
}
