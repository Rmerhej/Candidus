package fr.candidus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité de Candidus.
 * <ul>
 *     <li>Pages publiques : accueil, inscription, connexion.</li>
 *     <li>Tout le reste demande d'être connecté.</li>
 *     <li>Connexion par e-mail et mot de passe (haché avec BCrypt en base).</li>
 *     <li>Protection CSRF active : Thymeleaf ajoute le jeton caché dans chaque formulaire th:action.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/inscription", "/aide", "/mentions-legales", "/conditions",
                                "/confidentialite", "/js/**", "/css/**",
                                "/favicon.ico", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        // Toujours arriver sur le tableau de bord après la connexion
                        .defaultSuccessUrl("/tableau-de-bord", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Encodeur "délégué" : BCrypt par défaut, et prêt pour un changement d'algorithme
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
