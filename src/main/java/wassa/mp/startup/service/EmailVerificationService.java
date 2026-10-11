package wassa.mp.startup.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.model.EmailVerificationToken;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.EmailVerificationTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void envoyerLienVerification(User user) {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        LocalDateTime expiration =
                LocalDateTime.now().plusMinutes(30);

        EmailVerificationToken verificationToken =
                new EmailVerificationToken(token, user, expiration);

        tokenRepository.save(verificationToken);

        String lien = backendUrl
                + "/api/email/verify?token=" + token;

        String contenu = """
                Bonjour %s,

                Merci pour votre inscription sur Entrepreneur360.

                Pour vérifier votre adresse e-mail, cliquez sur le lien suivant :
                %s

                Ce lien expire dans 30 minutes.

                Si vous n'êtes pas à l'origine de cette inscription,
                ignorez ce message.
                """.formatted(user.getPrenom(), lien);

        emailService.envoyerEmail(
                user.getEmail(),
                "Vérification de votre adresse e-mail - Entrepreneur360",
                contenu
        );
    }
    @Transactional
    public void verifierEmail(String token) {

        EmailVerificationToken verificationToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lien de vérification invalide."
                                )
                        );

        if (verificationToken.getExpirationDate()
                .isBefore(LocalDateTime.now())) {

            tokenRepository.delete(verificationToken);

            throw new IllegalArgumentException(
                    "Le lien de vérification a expiré."
            );
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        tokenRepository.delete(verificationToken);
    }

}
