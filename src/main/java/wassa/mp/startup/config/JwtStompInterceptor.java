package wassa.mp.startup.config;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.service.ConversationService;
import wassa.mp.startup.service.JwtService;

@Component
public class JwtStompInterceptor implements ChannelInterceptor {
    private final ConversationService conversationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtStompInterceptor(
            JwtService jwtService,
            UserDetailsService userDetailsService,ConversationService conversationService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.conversationService = conversationService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Authentifier la connexion STOMP.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new BadCredentialsException(
                        "Jeton JWT absent de la connexion WebSocket."
                );
            }
            String jwt = authHeader.substring(7).trim();

            if (jwt.isEmpty()) {
                throw new BadCredentialsException(
                        "Jeton JWT vide."
                );
            }

            try {
                String email = jwtService.extraUserName(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new BadCredentialsException(
                            "Jeton JWT invalide ou expiré."
                    );
                }

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Associe l'utilisateur authentifié à la session STOMP.
                accessor.setUser(authentication);

            } catch (JwtException | IllegalArgumentException e) {
                throw new BadCredentialsException(
                        "Impossible de valider le jeton JWT.",
                        e
                );
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();

            if (destination == null || !destination.matches(
                    "^/topic/conversations/\\d+$")) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Abonnement WebSocket non autorisé."
                );
            }

            Authentication authentication = (Authentication) accessor.getUser();

            if (authentication == null || !(authentication.getPrincipal() instanceof CustumUserDetail userConnecte)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Utilisateur non authentifié."
                );
            }

            int conversationId = Integer.parseInt(destination.substring("/topic/conversations/".length()));

            // Refuse l'abonnement si le service refuse l'accès.
            try {
                conversationService.verifierAcces(conversationId, userConnecte);
            } catch (RuntimeException e) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Accès à cette conversation refusé.",
                        e
                );
            }
        }


        return message;
    }
}
