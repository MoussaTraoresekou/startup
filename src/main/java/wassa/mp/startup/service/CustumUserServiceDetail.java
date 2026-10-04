package wassa.mp.startup.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.model.Mentor;
import wassa.mp.startup.model.User;
import wassa.mp.startup.Enumeration.StatuEnumMentor;
import wassa.mp.startup.repository.MentorRepository;
import wassa.mp.startup.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;

@Component
public class CustumUserServiceDetail implements UserDetailsService {

    private final UserRepository userRepository;
    private final MentorRepository mentorRepository; // AJOUT : Pour vérifier le statut du mentor

    // Mise à jour du constructeur pour injecter les deux dépôts obligatoires
    public CustumUserServiceDetail(UserRepository userRepository, MentorRepository mentorRepository) {
        this.userRepository = userRepository;
        this.mentorRepository = mentorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("Utilisateur introuvable");
        }

        // RÈGLE MÉTIER INTERCEPTATRICE : Vérifier si l'utilisateur qui se connecte est un Mentor
        // L'héritage @Inheritance(strategy = InheritanceType.JOINED) permet de chercher directement par l'id du User
        Optional<Mentor> optionalMentor = mentorRepository.findById(user.getId());

        if (optionalMentor.isPresent()) {
            Mentor mentor = optionalMentor.get();

            // Cas 1 : Le compte est toujours bloqué en attente d'étude du dossier
            if (mentor.getStatuEnumMentor() == StatuEnumMentor.EN_ATTENTE) {
                throw new BadCredentialsException("Votre compte mentor est toujours en cours d'examen par nos administrateurs.");
            }

            // Cas 2 : L'administrateur a définitivement refusé les pièces justificatives
            if (mentor.getStatuEnumMentor() == StatuEnumMentor.REFUSEE) {
                throw new BadCredentialsException("Votre inscription a été déclinée par l'administration après examen de votre CV/Diplôme.");
            }
        }

        // Si l'utilisateur est un porteur ou un mentor VALIDE, l'authentification se poursuit normalement
        return new CustumUserDetail(user);
    }
}
