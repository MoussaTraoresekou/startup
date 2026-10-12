package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.Enumeration.RoleEnum;
import wassa.mp.startup.dto.MentorEnAttenteResponseDto;
import wassa.mp.startup.dto.MentorRegisterRequestDto;
import wassa.mp.startup.dto.MentorReponseDto;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Mentor;
import wassa.mp.startup.Enumeration.StatuEnumMentor;
import wassa.mp.startup.repository.MentorRepository;
import wassa.mp.startup.repository.SecteurActiviteRepository;

import java.util.List;

@Service
public class MentorService {
    @Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private EmailVerificationService emailVerificationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SecteurActiviteRepository secteurActiviteRepository;
    public MentorReponseDto inscrireMentor(MentorRegisterRequestDto dto) {

        Mentor mentor = new Mentor();
        mentor.setPrenom(dto.getPrenom());
        mentor.setNom(dto.getNom());
        mentor.setEmail(dto.getEmail());
        mentor.setRole(RoleEnum.MENTOR);
        mentor.setTelephone(dto.getTelephone());
        mentor.setPassword(passwordEncoder.encode(dto.getMotDePass()));
        mentor.setCv_url(dto.getCvUrl());
        mentor.setDiplome_url(dto.getDiplomeUrl());
        mentor.setSecteurActivite(secteurActiviteRepository.findById(dto.getSecteur()).get());
        mentor.setDescription(dto.getDescription());
        mentor.setStatuEnumMentor(StatuEnumMentor.EN_ATTENTE);

        Mentor m = mentorRepository.save(mentor);

        // Envoyer le lien de vérification au mentor
        emailVerificationService.envoyerLienVerification(m);

        return new MentorReponseDto(
                m.getId(),
                m.getNom(),
                m.getPrenom(),
                m.getDiplome_url(),
                m.getCv_url(),
                m.getStatuEnumMentor().toString(),
                m.getTelephone(),
                m.getRole().toString(),
                m.getEmail(),
                m.getDateCreation(),
                m.getDescription(),
                m.getSecteurActivite().getNom()!=null?m.getSecteurActivite().getNom():""
        );
    }
    public MentorReponseDto validerDossierMentor(int mentorId) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mentor introuvable avec l'ID : " + mentorId
                        )
                );

        if (mentor.getStatuEnumMentor() == StatuEnumMentor.ACCEPTEE) {
            throw new IllegalArgumentException(
                    "Ce compte mentor est déjà actif et validé."
            );
        }

        mentor.setStatuEnumMentor(StatuEnumMentor.ACCEPTEE);

        Mentor m = mentorRepository.save(mentor);

        emailService.envoyerEmailAcceptation(
                m.getEmail(),
                m.getPrenom()
        );

        return new MentorReponseDto(
                m.getId(),
                m.getNom(),
                m.getPrenom(),
                m.getDiplome_url(),
                m.getCv_url(),
                m.getStatuEnumMentor().toString(),
                m.getTelephone(),
                m.getRole().toString(),
                m.getEmail(),
                m.getDateCreation(),
                m.getDescription(),
                m.getSecteurActivite().getNom()
        );
    }

    public MentorReponseDto refuserDossierMentor(int mentorId) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mentor introuvable avec l'ID : " + mentorId
                        )
                );

        if (mentor.getStatuEnumMentor() == StatuEnumMentor.REFUSEE) {
            throw new IllegalArgumentException(
                    "Ce dossier mentor a déjà été refusé."
            );
        }

        mentor.setStatuEnumMentor(StatuEnumMentor.REFUSEE);

        Mentor m = mentorRepository.save(mentor);

        emailService.envoyerEmailRefus(
                m.getEmail(),
                m.getPrenom(),
                "Votre dossier ne répond pas actuellement aux critères de sélection."
        );

        return new MentorReponseDto(
                m.getId(),
                m.getNom(),
                m.getPrenom(),
                m.getDiplome_url(),
                m.getCv_url(),
                m.getStatuEnumMentor().toString(),
                m.getTelephone(),
                m.getRole().toString(),
                m.getEmail(),
                m.getDateCreation(),
                m.getDescription(),
                m.getSecteurActivite().getNom()
        );
    }
    public List<MentorEnAttenteResponseDto> listerMentorsEnAttente() {
        List<Mentor> mentorsEnAttente = mentorRepository.findByStatuEnumMentor(StatuEnumMentor.EN_ATTENTE);

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
    public List<MentorReponseDto> listerMentorsValides() {
        List<Mentor> mentorsEnAttente = mentorRepository.findByStatuEnumMentor(StatuEnumMentor.ACCEPTEE);

        return mentorsEnAttente.stream().map(m -> new MentorReponseDto(
                m.getId(),
                m.getNom(),
                m.getPrenom(),
                m.getDiplome_url(),
                m.getCv_url(),
                m.getStatuEnumMentor().toString(),
                m.getTelephone(),
                m.getRole().toString()==null?m.getRole().toString():"",
                m.getEmail(),
                m.getDateCreation(),
                m.getDescription(),
                m.getSecteurActivite().getNom()
        )).toList();
    }
    public List<MentorReponseDto> getAllMentors() {

        return mentorRepository.findAll().stream().map(m -> new MentorReponseDto(
                m.getId(),
                m.getNom(),
                m.getPrenom(),
                m.getDiplome_url(),
                m.getCv_url(),
                m.getStatuEnumMentor().toString(),
                m.getTelephone(),
                m.getRole().toString()==null?m.getRole().toString():"",
                m.getEmail(),
                m.getDateCreation(),
                m.getDescription(),
                m.getSecteurActivite().getNom()
                )).toList();
    }


}
