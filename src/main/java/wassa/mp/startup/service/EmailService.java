package wassa.mp.startup.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerEmail(String destinataire,
                             String sujet,
                             String contenu) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinataire);
        message.setSubject(sujet);
        message.setText(contenu);

        mailSender.send(message);
    }

    public void envoyerEmailAcceptation(String email, String nom) {
        String sujet = "Entrepreneur360 - Votre dossier mentor est accepté";

        String contenu = "Bonjour " + nom + ",\n\n"
                + "Nous avons le plaisir de vous informer que "
                + "votre dossier de candidature en tant que mentor "
                + "a été accepté par notre équipe.\n\n"
                + "Votre profil est désormais validé. "
                + "Vous pouvez vous connecter à Entrepreneur360.\n\n"
                + "Merci de rejoindre notre communauté.\n\n"
                + "L'équipe Entrepreneur360";

        envoyerEmail(email, sujet, contenu);
    }

    public void envoyerEmailRefus(String email, String nom, String motif) {
        String sujet = "Entrepreneur360 - Décision concernant votre dossier";

        String contenu = "Bonjour " + nom + ",\n\n"
                + "Après examen de votre dossier de candidature "
                + "en tant que mentor, nous sommes au regret de vous "
                + "informer que votre candidature n'a pas été retenue.\n\n"
                + "Motif : " + motif + "\n\n"
                + "Nous vous remercions pour l'intérêt porté "
                + "à Entrepreneur360.\n\n"
                + "L'équipe Entrepreneur360";

        envoyerEmail(email, sujet, contenu);
    }
}
