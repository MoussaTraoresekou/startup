package wassa.mp.startup.service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.RoleEnum;
import wassa.mp.startup.Enumeration.StatuEnumMentor;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.model.Mentor;
import wassa.mp.startup.model.User;
import wassa.mp.startup.repository.MentorRepository;
import wassa.mp.startup.repository.UserRepository;

@Service
public class CustumUserServiceDetail implements UserDetailsService {

    private final UserRepository userRepository;
    private final MentorRepository mentorRepository;

    public CustumUserServiceDetail(
            UserRepository userRepository,
            MentorRepository mentorRepository) {
        this.userRepository = userRepository;
        this.mentorRepository = mentorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "Utilisateur introuvable."
            );
        }

        // Vérifier l'e-mail avant de poursuivre l'authentification
        if (!user.isEmailVerified()) {
            throw new OperationInterditeException(
                    "Veuillez confirmer votre adresse e-mail avant de vous connecter."
            );
        }

        // Vérifier le statut uniquement pour les mentors
        if (user.getRole() == RoleEnum.MENTOR) {

            Mentor mentor = mentorRepository.findById(user.getId())
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Profil mentor introuvable."
                            )
                    );

            if (mentor.getStatuEnumMentor()
                    == StatuEnumMentor.EN_ATTENTE) {
                throw new OperationInterditeException(
                        "Votre compte mentor est en attente de validation par l'administration."
                );
            }

            if (mentor.getStatuEnumMentor()
                    == StatuEnumMentor.REFUSEE) {
                throw new OperationInterditeException(
                        "Votre inscription a été refusée par l'administration."
                );
            }
        }

        return new CustumUserDetail(user);
    }
}
