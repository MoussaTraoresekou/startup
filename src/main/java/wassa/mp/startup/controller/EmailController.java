package wassa.mp.startup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wassa.mp.startup.service.EmailService;
import wassa.mp.startup.service.EmailVerificationService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final EmailVerificationService verificationService;

    public EmailController(EmailService emailService, EmailVerificationService verificationService) {
        this.emailService = emailService;
        this.verificationService = verificationService;
    }

    @PostMapping("/test")
    public String testerEmail(
            @RequestParam String destinataire) {

        emailService.envoyerEmail(
                destinataire,
                "Test Entrepreneur360",
                "Ceci est un e-mail de test."
        );

        return "E-mail envoyé avec succès !";
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifierEmail(@RequestParam String token) {

        try {
            verificationService.verifierEmail(token);

            return ResponseEntity.ok(
                    "Votre adresse e-mail est vérifiée avec succès. "
                            + "Vous pouvez maintenant vous connecter."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    e.getMessage()
            );
        }
    }
}
