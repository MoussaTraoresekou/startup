package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.dto.MentorEnAttenteResponseDto;
import wassa.mp.startup.dto.MentorRegisterRequestDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Mentor;
import wassa.mp.startup.Enumeration.StatuEnumMentor;
import wassa.mp.startup.repository.MentorRepository;

import java.util.List;

@Service
public class MentorService {
    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void inscrireMentor(MentorRegisterRequestDto dto) {

        // Sécurité : Vérifier si l'email n'est pas déjà utilisé dans le système
        // (À adapter selon votre UserRepository/MentorRepository)

        Mentor mentor = new Mentor();
        // Données héritées de la classe User
        mentor.setPrenom(dto.getPrenom());
        mentor.setNom(dto.getNom());
        mentor.setEmail(dto.getEmail());
        mentor.setTelephone(dto.getTelephone());
        // Encodage sécurisé du mot de passe avec le sel configuré (14)
        mentor.setPassword(passwordEncoder.encode(dto.getMotDePass()));

        // Données spécifiques au profil Mentor
        mentor.setCv_url(dto.getCvUrl());
        mentor.setDiplome_url(dto.getDiplomeUrl());

        // RÈGLE MÉTIER : Le compte est bloqué en attente de vérification administrative
        mentor.setStatuEnumMentor(StatuEnumMentor.EN_ATTENTE);

        mentorRepository.save(mentor);
    }
    /**
     * Permet à l'administrateur de valider un dossier mentor et d'activer son accès
     */
    @Transactional
    public void validerDossierMentor(int mentorId) {
        // 1. Récupération du mentor par son ID
        Mentor mentor = mentorRepository.findById(mentorId).orElseThrow(
                () -> new ResourceNotFoundException("Mentor introuvable avec l'ID : " + mentorId)
        );

        // 2. Vérification de sécurité métier
        if (mentor.getStatuEnumMentor() == StatuEnumMentor.ACCEPTEE) {
            throw new IllegalArgumentException("Ce compte mentor est déjà actif et validé.");
        }

        // 3. Changement de statut vers VALIDE (ce qui débloquera automatiquement le login)
        mentor.setStatuEnumMentor(StatuEnumMentor.ACCEPTEE);

        mentorRepository.save(mentor);
    }
    /**
     * Permet à l'administrateur de refuser un dossier mentor si les documents ne sont pas conformes
     */
    @Transactional
    public void refuserDossierMentor(int mentorId) {
        // 1. Récupération du mentor par son ID
        Mentor mentor = mentorRepository.findById(mentorId).orElseThrow(
                () -> new ResourceNotFoundException("Mentor introuvable avec l'ID : " + mentorId)
        );

        // 2. Vérification de sécurité métier
        if (mentor.getStatuEnumMentor() == StatuEnumMentor.REFUSEE) {
            throw new IllegalArgumentException("Ce compte mentor a déjà été marqué comme REFUSE.");
        }

        // 3. Changement de statut vers REFUSE
        mentor.setStatuEnumMentor(StatuEnumMentor.REFUSEE);

        mentorRepository.save(mentor);
    }
    /**
     * Liste tous les mentors dont le dossier d'inscription est toujours en attente
     */
    @Transactional(readOnly = true)
    public List<MentorEnAttenteResponseDto> listerMentorsEnAttente() {
        List<Mentor> mentorsEnAttente = mentorRepository.findByStatuEnumMentor(StatuEnumMentor.EN_ATTENTE);

        // Transformation de la liste d'entités en liste de DTOs sécurisés
        return mentorsEnAttente.stream().map(m -> new MentorEnAttenteResponseDto(
                m.getId(),
                m.getPrenom(),
                m.getNom(),
                m.getEmail(),
                m.getTelephone(),
                m.getCv_url(),
                m.getDiplome_url()
        )).toList();
    }


}
