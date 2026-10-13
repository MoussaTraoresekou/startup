package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.model.Reponse;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.MentorProjetRepository;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProjetEtapeService {
    @Autowired
    private ProjetEtapeRepository projetEtapeRepository;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private EtapeRepository etapeRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MentorProjetRepository mentorProjetRepository;

    public List<ProjetEtapeResponseDto> projetEtapeResponseDtoList(int projetId,@AuthenticationPrincipal CustumUserDetail userConnecter) {
        Projet projet=projetRepository.findById(projetId).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        if(projet.getPorteur().getId()!=userConnecter.getUser().getId()){
            throw new NonAutoriseException("non autorisé");
        }
        return projetEtapeRepository.findByProjetId(projetId).stream().map(
                 projetEtape -> new ProjetEtapeResponseDto(
                         projetEtape.getId(),projetEtape.getStatutEtape(),projetEtape.getDateSoumission(),
                         projetEtape.getDateValidation(),projetEtape.getCommentaireMentor(),projetEtape.getEtape().getType().toString(),null
                 )
        ).toList();
    }
    public List<ProjetEtapeResponseDto> projetEtapeResponseDtoListPourMentor(int projetId,@AuthenticationPrincipal CustumUserDetail userConnecter) {
        Projet projet=projetRepository.findById(projetId).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        return projetEtapeRepository.findByProjetId(projetId).stream().map(
                projetEtape -> new ProjetEtapeResponseDto(
                        projetEtape.getId(),projetEtape.getStatutEtape(),projetEtape.getDateSoumission(),
                        projetEtape.getDateValidation(),projetEtape.getCommentaireMentor(),projetEtape.getEtape().getType().toString(),null
                )
        ).toList();
    }
    /*
    public ProjetEtape commencerEtapes(ProjetEtapesRequestDto projetEtapesRequestDto,@AuthenticationPrincipal CustumUserDetail userConnecter) {
        Projet projet=projetRepository.findById(projetEtapesRequestDto.getProjetId()).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        if(projet.getPorteur().getId()!=userConnecter.getUser().getId()){
            throw new NonAutoriseException("non autorisé");
        }
        Optional<ProjetEtape>  projetEtapeVerif=projetEtapeRepository.findByProjetIdAndEtapeId(projet.getId(), projetEtapesRequestDto.getEtape_id());
        if(projetEtapeVerif.isPresent()){
            ProjetEtape projetEtapeExistant=projetEtapeVerif.get();
            projetEtapeExistant.setProjet(projet);
            projetEtapeExistant.setEtape(etapeRepository.findById(projetEtapesRequestDto.getEtape_id()).get());
            return projetEtapeRepository.save(projetEtapeExistant);


        }else{
            ProjetEtape projetEtape=new ProjetEtape();
            projetEtape.setStatutEtape(StatutEtapeEnum.EN_COUR);
            projetEtape.setProjet(projet);
            projetEtape.setEtape(etapeRepository.findById(projetEtapesRequestDto.getEtape_id()).get());
            return projetEtapeRepository.save(projetEtape);
        }

    }

     */
    public ProjetEtape commencerEtapes(ProjetEtapesRequestDto projetEtapesRequestDto, @AuthenticationPrincipal CustumUserDetail userConnecter) {
        Projet projet = projetRepository.findById(projetEtapesRequestDto.getProjetId()).orElseThrow(
                () -> new ResourceNotFoundException("Projet n'existe pas")
        );
        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("non autorisé");
        }
        Optional<ProjetEtape> projetEtapeVerif = projetEtapeRepository.findByProjetIdAndEtapeId(
                projet.getId(),
                projetEtapesRequestDto.getEtape_id()
        );
        if (projetEtapeVerif.isPresent()) {
            return projetEtapeVerif.get();
        } else {
            ProjetEtape projetEtape = new ProjetEtape();
            projetEtape.setStatutEtape(StatutEtapeEnum.EN_COUR);
            projetEtape.setProjet(projet);
            Etape etape = etapeRepository.findById(projetEtapesRequestDto.getEtape_id()).orElseThrow(
                    () -> new ResourceNotFoundException("L'étape générique demandée n'existe pas")
            );
            projetEtape.setEtape(etape);
            return projetEtapeRepository.save(projetEtape);
        }
    }

    public byte[] genererDocumentLivrable(int projetEtapeId, CustumUserDetail userConnecter) {
        // 1. Récupération de l'étape du projet
        ProjetEtape projetEtape = projetEtapeRepository.findById(projetEtapeId).orElseThrow(
                () -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas")
        );

        // 2. Sécurité : Vérifier que le projet appartient bien à l'utilisateur connecté
        if (projetEtape.getProjet().getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas autorisé à accéder aux données de ce projet.");
        }

        // 3. VOTRE RÈGLE MÉTIER : Vérifier si toutes les questions ont reçu une réponse
        int nombreQuestionsTotale = projetEtape.getEtape().getQuestions().size();
        int nombreReponsesDonnees = projetEtape.getReponse().size();

        if (nombreReponsesDonnees < nombreQuestionsTotale) {
            throw new OperationInterditeException("Impossible de télécharger le livrable : vous devez répondre à toutes les questions de l'étape ("
                    + nombreReponsesDonnees + "/" + nombreQuestionsTotale + " répondues).");
        }

        // 4. Construction de la structure textuelle du document (Le téléchargement est autorisé !)
        StringBuilder doc = new StringBuilder();
        doc.append("======================================================================\n");
        doc.append("                      LIVRABLE DE FIN D'ÉTAPE                         \n");
        doc.append("======================================================================\n\n");
        doc.append("🏢 PROJET : ").append(projetEtape.getProjet().getTitre().toUpperCase()).append("\n");
        doc.append("📍 ÉTAPE  : ").append(projetEtape.getEtape().getType()).append("\n");
        doc.append("📅 STATUT ACTUEL : ").append(projetEtape.getStatutEtape()).append("\n"); // Affiche si c'est EN_COURS, EN_ATTENTE ou TERMINE

        if (projetEtape.getCommentaireMentor() != null) {
            doc.append("💬 COMMENTAIRE DU MENTOR : ").append(projetEtape.getCommentaireMentor()).append("\n");
        }
        doc.append("\n----------------------------------------------------------------------\n");
        doc.append("                         QUESTIONNAIRE REMPLI                         \n");
        doc.append("----------------------------------------------------------------------\n\n");

        // 5. Boucle pour lister chaque question et sa réponse associée
        List<Reponse> reponses = projetEtape.getReponse();
        for (Reponse r : reponses) {
            doc.append("❓ QUESTION : ").append(r.getQuestion().getLibelle()).append("\n");
            doc.append("✍️ RÉPONSE  : ").append(r.getReponse()).append("\n");
            doc.append("\n----------------------------------------------------------------------\n");
        }

        return doc.toString().getBytes(StandardCharsets.UTF_8);
    }
    /*
    @Transactional
    public void soumettreLivrableEtape(int projetEtapeId, CustumUserDetail userConnecter) {
        ProjetEtape pe = projetEtapeRepository.findById(projetEtapeId)
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        if (pe.getProjet().getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas autorisé à soumettre ce projet.");
        }

        int totalQuestions = pe.getEtape().getQuestions().size();
        int totalReponses = pe.getReponse().size();

        if (totalReponses < totalQuestions) {
            throw new OperationInterditeException("Veuillez répondre à toutes les questions avant de soumettre ("
                    + totalReponses + "/" + totalQuestions + " répondues).");
        }

        pe.setStatutEtape(StatutEtapeEnum.SOUMIS);
        pe.setDateSoumission(LocalDate.now());

        projetEtapeRepository.save(pe);
    }
    @Transactional
    public void validerEtapeParMentor(ActionEtapeDto dto) {
        ProjetEtape pe = projetEtapeRepository.findById(dto.getProjetEtapeId())
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        pe.setStatutEtape(StatutEtapeEnum.VALIDE);
        pe.setDateValidation(LocalDate.now());
        pe.setCommentaireMentor(dto.getCommentaireMentor());

        projetEtapeRepository.save(pe);
    }
    @Transactional
    public void renvoyerEtapePourCorrection(ActionEtapeDto dto) {
        ProjetEtape pe = projetEtapeRepository.findById(dto.getProjetEtapeId())
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        pe.setStatutEtape(StatutEtapeEnum.A_REVOIR);
        pe.setCommentaireMentor(dto.getCommentaireMentor());
        pe.setDateValidation(null); // On s'assure que la date de validation reste vide

        projetEtapeRepository.save(pe);
    }

     */

    @Transactional
    public void soumettreLivrableEtape(int projetEtapeId, CustumUserDetail userConnecter) {
        ProjetEtape pe = projetEtapeRepository.findById(projetEtapeId)
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        if (pe.getProjet().getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas autorisé à soumettre ce projet.");
        }

        // 🚀 CORRECTIF : On vérifie que le statut actuel permet bien une soumission (soit EN_COUR, soit A_CORRIGER)
        if (pe.getStatutEtape() == StatutEtapeEnum.SOUMIS || pe.getStatutEtape() == StatutEtapeEnum.VALIDE) {
            throw new OperationInterditeException("Cette étape a déjà été soumise ou validée.");
        }

        // Sécurité sur le nombre de questions uniques ayant reçu une réponse
        int totalQuestions = pe.getEtape().getQuestions().size();

        // 🚀 AMÉLIORATION : On compte le nombre de questions UNIQUES qui ont eu une réponse pour éviter les doublons de lignes SQL
        long questionsRepondues = pe.getReponse().stream()
                .map(r -> r.getQuestion().getId())
                .distinct()
                .count();

        if (questionsRepondues < totalQuestions) {
            throw new OperationInterditeException("Veuillez répondre à toutes les questions avant de soumettre ("
                    + questionsRepondues + "/" + totalQuestions + " complétées).");
        }

        pe.setStatutEtape(StatutEtapeEnum.SOUMIS);
        pe.setDateSoumission(LocalDate.now());

        ProjetEtape saved = projetEtapeRepository.save(pe);

        // 🚀 CORRECTIF : On caste et on type explicitement en (MentorProjet) pour débloquer le getMentor()
        mentorProjetRepository.findByProjetIdAndStatut(saved.getProjet().getId(), StatutDemandeMentoratEnum.ACCEPTEE)
                .ifPresent(obj -> {
                    wassa.mp.startup.model.MentorProjet liaison = (wassa.mp.startup.model.MentorProjet) obj;

                    String alerteMsg = "Le projet " + saved.getProjet().getTitre() + " a soumis l'étape : " + saved.getEtape().getType();
                    AlerteMentoratDto alerteDto = new AlerteMentoratDto(
                            saved.getId(),
                            alerteMsg,
                            saved.getProjet().getPorteur().getPrenom(),
                            "PORTEUR",
                            saved.getProjet().getTitre(),
                            java.time.LocalDateTime.now()
                    );
                    notificationService.notifierUtilisateur(liaison.getMentor(), alerteDto);
                });
    }


    @Transactional
    public void validerEtapeParMentor(ActionEtapeDto dto) {
        ProjetEtape pe = projetEtapeRepository.findById(dto.getProjetEtapeId())
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        pe.setStatutEtape(StatutEtapeEnum.VALIDE);
        pe.setDateValidation(LocalDate.now());
        pe.setCommentaireMentor(dto.getCommentaireMentor());
        ProjetEtape saved = projetEtapeRepository.save(pe);

        String alerteMsg = "Félicitations ! Votre mentor a validé l'étape : " + saved.getEtape().getType();
        AlerteMentoratDto alerteDto = new AlerteMentoratDto(
                saved.getId(),
                alerteMsg,
                "Système",
                "MENTOR",
                saved.getProjet().getTitre(),
                java.time.LocalDateTime.now()
        );
        notificationService.notifierUtilisateur(saved.getProjet().getPorteur(), alerteDto);
    }


    @Transactional
    public void renvoyerEtapePourCorrection(ActionEtapeDto dto) {
        ProjetEtape pe = projetEtapeRepository.findById(dto.getProjetEtapeId())
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        pe.setStatutEtape(StatutEtapeEnum.A_REVOIR);
        pe.setCommentaireMentor(dto.getCommentaireMentor());
        pe.setDateValidation(null);
        ProjetEtape saved = projetEtapeRepository.save(pe);

        String alerteMsg = "Des corrections sont attendues sur l'étape : " + saved.getEtape().getType() + ". Motif : " + dto.getCommentaireMentor();
        AlerteMentoratDto alerteDto = new AlerteMentoratDto(
                saved.getId(),
                alerteMsg,
                "Système",
                "MENTOR",
                saved.getProjet().getTitre(),
                java.time.LocalDateTime.now()
        );
        notificationService.notifierUtilisateur(saved.getProjet().getPorteur(), alerteDto);
    }


    @Transactional(readOnly = true)
    public LivrableEtapeDto consulterLivrableEnJson(int projetEtapeId) {
        ProjetEtape pe = projetEtapeRepository.findById(projetEtapeId)
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        List<LivrableEtapeDto.QuestionReponseDto> elements = pe.getReponse().stream().map(
                r -> new LivrableEtapeDto.QuestionReponseDto(r.getQuestion().getLibelle(), r.getReponse())
        ).toList();

        return new LivrableEtapeDto(
                pe.getProjet().getTitre(),
                pe.getEtape().getType().toString(),
                pe.getProjet().getPorteur().getPrenom() + " " + pe.getProjet().getPorteur().getNom(),
                elements
        );
    }



}
