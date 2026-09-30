package wassa.mp.startup.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import wassa.mp.startup.service.JwtService;
import java.io.IOException;

@Component
public class JwtAuthentificationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Récupérer le header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Vérifier que le header existe et commence par Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Récupérer uniquement le JWT
        String jwt = authHeader.substring(7);

        try {

            // 4. Récupérer le username depuis le JWT
            String email = jwtService.extraUserName(jwt);

            // 5. Vérifier si l'utilisateur n'est pas déjà authentifié
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Charger l'utilisateur depuis la base
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // 7. Vérifier le JWT
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 8. Créer l'authentification
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 9. Ajouter les détails de la requête
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 10. Enregistrer l'utilisateur comme authentifié
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authenticationToken);
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur JWT : " + e.getMessage());
        }

        // 11. Continuer la chaîne
        filterChain.doFilter(request, response);
    }
}