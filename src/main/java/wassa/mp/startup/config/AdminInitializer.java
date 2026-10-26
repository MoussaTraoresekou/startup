        package wassa.mp.startup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wassa.mp.startup.Enumeration.RoleEnum;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.UserRepository;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${app.admin.nom:Administrateur}")
    private String nom;

    @Value("${app.admin.prenom:Entrepreneur360}")
    private String prenom;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.telephone}")
    private String telephone;

    @Value("${app.admin.password}")
    private String password;

    public AdminInitializer(
            UserRepository userRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // Vérifier si un compte utilise déjà cette adresse e-mail.
        User utilisateurExistant = userRepository.findByEmail(email);

        if (utilisateurExistant != null) {

            // Un compte existe déjà : ne pas le recréer.
            if (utilisateurExistant.getRole() == RoleEnum.ADMIN) {
                System.out.println(
                        "Le compte administrateur creee. "

                );
                return;
            }

            // L'adresse appartient à un compte qui n'est pas administrateur.
            throw new IllegalStateException(
                    "L'adresse e-mail configurée pour l'administrateur "
                            + "est déjà utilisée par un compte non administrateur. "
                            + "Veuillez configurer une autre adresse."
            );
        }

        // Aucun compte n'utilise cette adresse : créer l'administrateur.
        User admin = new User();

        admin.setNom(nom);
        admin.setPrenom(prenom);
        admin.setEmail(email);
        admin.setTelephone(telephone);

        // Hacher le mot de passe avant l'enregistrement.
        admin.setPassword(bCryptPasswordEncoder.encode(password));

        admin.setRole(RoleEnum.ADMIN);
        admin.setEmailVerified(true);

        userRepository.save(admin);

        System.out.println(
                "Le compte administrateur a été créé avec succès."
        );
    }
}
