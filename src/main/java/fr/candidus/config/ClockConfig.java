package fr.candidus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * L'horloge de l'application, à l'heure de Paris.
 * Injectée plutôt qu'appelée directement (LocalDate.now()) : les tests peuvent fixer la date.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Paris"));
    }
}
