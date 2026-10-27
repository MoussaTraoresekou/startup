
package wassa.mp.startup.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthentificationFilter jwtAuthentificationFilter;

    public SecurityConfig(
            UserDetailsService userDetailsService,
            JwtAuthentificationFilter jwtAuthentificationFilter) {

        this.userDetailsService = userDetailsService;
        this.jwtAuthentificationFilter = jwtAuthentificationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(csrf -> csrf.disable())

                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.setCharacterEncoding("UTF-8");

                                    response.getWriter().write(
                                            "{\"status\":401,\"message\":\"Authentification requise ou session expirée.\"}"
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.setStatus(
                                            HttpServletResponse.SC_FORBIDDEN
                                    );
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.setCharacterEncoding("UTF-8");

                                    response.getWriter().write(
                                            "{\"status\":403,\"message\":\"Vous n'avez pas les droits nécessaires pour accéder à cette ressource.\"}"
                                    );
                                }
                        )
                )

                .authorizeHttpRequests(request -> request

                        // Routes accessibles sans authentification.
                        .requestMatchers(
                                "/favicon.ico",
                                "/api/user/login",
                                "/api/porteur/register",
                                "/api/investisseur/register",
                                "/api/mentor/register",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/email/**",
                                "/api/secteur/**",
                                "/api/specialites/**",

                                // Négociation initiale WebSocket.
                                "/ws",
                                "/ws/**"
                        ).permitAll()

                        // Routes réservées aux rôles concernés.
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/porteur/**")
                        .hasRole("PORTEUR")

                        .requestMatchers("/api/mentor/**")
                        .hasRole("MENTOR")

                        .requestMatchers("/api/collaborateur/**")
                        .hasRole("COLLABORATEUR")

                        .requestMatchers("/api/investisseur/**")
                        .hasRole("INVESTISSEUR")

                        // Routes réservées aux utilisateurs authentifiés.
                        .requestMatchers("/api/notifications/**")
                        .authenticated()

                        .requestMatchers("/api/conversations/**")
                        .authenticated()

                        // Les fichiers restent protégés.
                        .requestMatchers("/api/fichiers/**")
                        .authenticated()

                        // Toute autre route exige une authentification.
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtAuthentificationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Configuration permissive pour le développement.
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Range"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization",
                        "Accept-Ranges",
                        "Content-Range",
                        "Content-Length"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(14);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(bCryptPasswordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}
