package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.Enumeration.RoleEnum;
import wassa.mp.startup.dto.CollaborateurRequestDto;
import wassa.mp.startup.dto.CollaborateurResponseDto;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.model.Collaborateur;
import wassa.mp.startup.repository.CollaborateurRepository;
import wassa.mp.startup.repository.UserRepository;

@Service
public class CollaborateurService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollaborateurRepository collaborateurRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Transactional
    public CollaborateurResponseDto inscrireCollaborateur(CollaborateurRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new OperationInterditeException("Cette adresse e-mail est déjà utilisée.");
        }

        Collaborateur collab = new Collaborateur();

        collab.setNom(dto.getNom());
        collab.setPrenom(dto.getPrenom());
        collab.setTelephone(dto.getTelephone());
        collab.setEmail(dto.getEmail());
        collab.setPassword(passwordEncoder.encode(dto.getPassword()));
        collab.setRole(RoleEnum.COLLABORATEUR);

        collab.setSpecialite(dto.getSpecialite());
        collab.setBiographie(dto.getBiographie());
        collab.setLienPortfolio(dto.getLienPortfolio());
        collab.setDisponible(true);

        Collaborateur saved = collaborateurRepository.save(collab);
        // Envoyer le lien de vérification
        emailVerificationService.envoyerLienVerification(saved);

        return new CollaborateurResponseDto(
                saved.getId(),
                saved.getNom(),
                saved.getPrenom(),
                saved.getTelephone(),
                saved.getEmail(),
                saved.getSpecialite(),
                saved.getBiographie(),
                saved.getLienPortfolio(),
                saved.isDisponible()
        );
    }
}
