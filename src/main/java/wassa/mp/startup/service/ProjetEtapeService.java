package wassa.mp.startup.service;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.StatutDemandeMentoratEnum;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import wassa.mp.startup.dto.*;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.*;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.MentorProjetRepository;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public List<ProjetEtapeResponseDto> projetEtapeResponseDtoList(int projetId, CustumUserDetail userConnecter) {
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
    public List<ProjetEtapeResponseDto> projetEtapeResponseDtoListPourMentor(int projetId, CustumUserDetail userConnecter) {
        Projet projet=projetRepository.findById(projetId).orElseThrow(
                ()-> new ResourceNotFoundException("Projet n'existe pas")
        );
        verifierMentorAffecte(projetId, userConnecter);
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
    public ProjetEtape commencerEtapes(ProjetEtapesRequestDto projetEtapesRequestDto,  CustumUserDetail userConnecter) {
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


    public byte[] genererDocumentLivrablelaisser(int projetEtapeId, CustumUserDetail userConnecter) {
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
/*
    @Transactional
    public void soumettreLivrableEtape(int projetEtapeId, CustumUserDetail userConnecter) {
        ProjetEtape pe = projetEtapeRepository.findById(projetEtapeId)
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        if (pe.getProjet().getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas autorisé à soumettre ce projet.");
        }

        if (pe.getStatutEtape() == StatutEtapeEnum.SOUMIS || pe.getStatutEtape() == StatutEtapeEnum.VALIDE) {
            throw new OperationInterditeException("Cette étape a déjà été soumise ou validée.");
        }

        // Sécurité sur le nombre de questions uniques ayant reçu une réponse
        int totalQuestions = pe.getEtape().getQuestions().size();

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

        //  On caste et on type explicitement en (MentorProjet) pour débloquer le getMentor()
        mentorProjetRepository.findByProjetIdAndStatut(saved.getProjet().getId(), StatutDemandeMentoratEnum.ACCEPTEE)
                .ifPresent(obj -> {
                    MentorProjet liaison = (MentorProjet) obj;

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



 */
    @Transactional
    public void soumettreLivrableEtape(int projetEtapeId, CustumUserDetail userConnecter) {
        ProjetEtape pe = projetEtapeRepository.findById(projetEtapeId)
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));

        if (pe.getProjet().getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas autorisé à soumettre ce projet.");
        }

        if (pe.getStatutEtape() == StatutEtapeEnum.SOUMIS || pe.getStatutEtape() == StatutEtapeEnum.VALIDE) {
            throw new OperationInterditeException("Cette étape a déjà été soumise ou validée.");
        }

        int totalQuestions = pe.getEtape().getQuestions().size();

        long questionsRepondues = pe.getReponse().stream()
                .map(r -> r.getQuestion().getId())
                .distinct()
                .count();

        if (questionsRepondues < totalQuestions) {
            throw new OperationInterditeException("Veuillez répondre à toutes les questions avant de soumettre ("
                    + questionsRepondues + "/" + totalQuestions + " complétées).");
        }

        List<MentorProjet> liaisonsMentors = mentorProjetRepository.findAllByProjetIdAndStatut(pe.getProjet().getId(), StatutDemandeMentoratEnum.ACCEPTEE);
        if (liaisonsMentors.isEmpty()) {
            throw new OperationInterditeException(" aucun mentor n'est affecté à ce projet pour le moment. veuillez  entrer  en contact avec un");
        }

        pe.setStatutEtape(StatutEtapeEnum.SOUMIS);
        pe.setDateSoumission(LocalDate.now());

        ProjetEtape saved = projetEtapeRepository.save(pe);

        String alerteMsg = "Le projet " + saved.getProjet().getTitre() + " a soumis l'étape : " + saved.getEtape().getType();
        AlerteMentoratDto alerteDto = new AlerteMentoratDto(
                saved.getId(),
                alerteMsg,
                saved.getProjet().getPorteur().getPrenom(),
                "PORTEUR",
                saved.getProjet().getTitre(),
                java.time.LocalDateTime.now()
        );

        for (MentorProjet mentorProjet : liaisonsMentors) {
            notificationService.notifierUtilisateur(mentorProjet.getMentor(), alerteDto);
        }
    }


    @Transactional
    public void validerEtapeParMentor(ActionEtapeDto dto,CustumUserDetail userConnecter
    ) {
        ProjetEtape pe = projetEtapeRepository.findById(dto.getProjetEtapeId())
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));
        verifierMentorAffecte(
                pe.getProjet().getId(),
                userConnecter
        );
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
    public void renvoyerEtapePourCorrection(ActionEtapeDto dto,CustumUserDetail userConnecter) {
        ProjetEtape pe = projetEtapeRepository.findById(dto.getProjetEtapeId())
                .orElseThrow(() -> new ResourceNotFoundException("L'étape de projet demandée n'existe pas."));
        verifierMentorAffecte(
                pe.getProjet().getId(),
                userConnecter
        );
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

/*
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

 */

    @Transactional(readOnly = true)
    public LivrableEtapeDto consulterLivrableEnJson(
            int projetEtapeId,
            CustumUserDetail userConnecter
    ) {
        ProjetEtape pe = projetEtapeRepository.findById(projetEtapeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "L'étape de projet demandée n'existe pas."
                        )
                );

        verifierMentorAffecte(
                pe.getProjet().getId(),
                userConnecter
        );

        List<LivrableEtapeDto.QuestionReponseDto> elements =
                pe.getReponse().stream()
                        .map(r ->
                                new LivrableEtapeDto.QuestionReponseDto(
                                        r.getQuestion().getLibelle(),
                                        r.getReponse()
                                )
                        )
                        .toList();

        return new LivrableEtapeDto(
                pe.getProjet().getTitre(),
                pe.getEtape().getType().toString(),
                pe.getProjet().getPorteur().getPrenom()
                        + " "
                        + pe.getProjet().getPorteur().getNom(),
                elements
        );
    }


    private void verifierMentorAffecte(
            int projetId,
            CustumUserDetail userConnecter
    ) {
        if (userConnecter == null || userConnecter.getUser() == null) {
            throw new NonAutoriseException(
                    "Vous devez être connecté pour effectuer cette action."
            );
        }

        int mentorId = userConnecter.getUser().getId();

        mentorProjetRepository
                .findByMentorIdAndProjetIdAndStatut(
                        mentorId,
                        projetId,
                        StatutDemandeMentoratEnum.ACCEPTEE
                )
                .orElseThrow(() ->
                        new NonAutoriseException(
                                "Vous n'êtes pas un mentor accepté pour ce projet."
                        )
                );
    }

    public byte[] genererDocumentLivrable(
            int projetEtapeId,
            CustumUserDetail userConnecter) {

        // 1. Récupérer l'étape du projet
        ProjetEtape projetEtape = projetEtapeRepository
                .findById(projetEtapeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "L'étape de projet demandée n'existe pas."
                        )
                );

        // 2. Vérifier que l'utilisateur est le porteur du projet
        if (projetEtape.getProjet().getPorteur().getId()
                != userConnecter.getUser().getId()) {

            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à télécharger le livrable de ce projet."
            );
        }

        // 3. Récupérer les questions et les réponses de cette étape uniquement
        var questions = projetEtape.getEtape().getQuestions();
        List<Reponse> reponses = projetEtape.getReponse();

        // 4. Vérifier que chaque question possède une réponse non vide
        List<String> questionsSansReponse = new ArrayList<>();

        for (var question : questions) {

            boolean repondu = reponses.stream().anyMatch(r ->
                    Objects.equals(
                            r.getQuestion().getId(),
                            question.getId()
                    )
                            && r.getReponse() != null
                            && !r.getReponse().isBlank()
            );

            if (!repondu) {
                questionsSansReponse.add(question.getLibelle());
            }
        }

        if (!questionsSansReponse.isEmpty()) {
            throw new OperationInterditeException(
                    "Le livrable ne peut pas être généré. "
                            + questionsSansReponse.size()
                            + " question(s) restent sans réponse."
            );
        }

        // 5. Créer le véritable document Word
        try (
                XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {

            // En-tête du document
            XWPFHeader header =
                    document.createHeader(HeaderFooterType.DEFAULT);

            XWPFParagraph paragrapheEntete =
                    header.createParagraph();

            paragrapheEntete.setAlignment(
                    ParagraphAlignment.RIGHT
            );

            XWPFRun texteEntete = paragrapheEntete.createRun();

            texteEntete.setText("ENTREPRENEUR360");
            texteEntete.setBold(true);
            texteEntete.setFontFamily("Arial");
            texteEntete.setFontSize(9);
            texteEntete.setColor("047857");

            // Titre principal
            XWPFParagraph titre = document.createParagraph();

            titre.setAlignment(ParagraphAlignment.CENTER);
            titre.setSpacingBefore(500);
            titre.setSpacingAfter(120);

            XWPFRun texteTitre = titre.createRun();

            texteTitre.setText("LIVRABLE DE FIN D'ÉTAPE");
            texteTitre.setBold(true);
            texteTitre.setFontFamily("Arial");
            texteTitre.setFontSize(21);
            texteTitre.setColor("047857");

            // Sous-titre
            XWPFParagraph sousTitre = document.createParagraph();

            sousTitre.setAlignment(ParagraphAlignment.CENTER);
            sousTitre.setSpacingAfter(350);

            XWPFRun texteSousTitre = sousTitre.createRun();

            texteSousTitre.setText(
                    "QUESTIONNAIRE ET SYNTHÈSE DES RÉPONSES"
            );

            texteSousTitre.setFontFamily("Arial");
            texteSousTitre.setFontSize(11);
            texteSousTitre.setColor("64748B");

            // Informations générales
            ajouterLigneInformation(
                    document,
                    "Projet",
                    projetEtape.getProjet().getTitre()
            );

            ajouterLigneInformation(
                    document,
                    "Étape",
                    String.valueOf(projetEtape.getEtape().getType())
            );

            ajouterLigneInformation(
                    document,
                    "Statut",
                    String.valueOf(projetEtape.getStatutEtape())
            );

            ajouterLigneInformation(
                    document,
                    "Date de génération",
                    LocalDate.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    )
            );

            // Séparation avant le questionnaire
            XWPFParagraph separation = document.createParagraph();

            separation.setSpacingBefore(150);
            separation.setSpacingAfter(150);

            separation.setBorderBottom(Borders.SINGLE);

            // Titre de la section
            XWPFParagraph titreQuestionnaire =
                    document.createParagraph();

            titreQuestionnaire.setSpacingBefore(180);
            titreQuestionnaire.setSpacingAfter(220);
            titreQuestionnaire.setKeepNext(true);

            XWPFRun texteQuestionnaire =
                    titreQuestionnaire.createRun();

            texteQuestionnaire.setText(
                    "RÉPONSES AU QUESTIONNAIRE"
            );

            texteQuestionnaire.setBold(true);
            texteQuestionnaire.setFontFamily("Arial");
            texteQuestionnaire.setFontSize(15);
            texteQuestionnaire.setColor("047857");

            // 6. Parcourir les questions dans l'ordre de l'étape
            int numero = 1;

            for (var question : questions) {

                Reponse reponse = reponses.stream()
                        .filter(r ->
                                Objects.equals(
                                        r.getQuestion().getId(),
                                        question.getId()
                                )
                        )
                        .filter(r ->
                                r.getReponse() != null
                                        && !r.getReponse().isBlank()
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new OperationInterditeException(
                                        "Une question ne possède pas de réponse."
                                )
                        );

                // Numéro et intitulé de la question
                XWPFParagraph paragrapheQuestion =
                        document.createParagraph();

                paragrapheQuestion.setSpacingBefore(220);
                paragrapheQuestion.setSpacingAfter(100);
                paragrapheQuestion.setKeepNext(true);

                XWPFRun texteQuestion =
                        paragrapheQuestion.createRun();

                texteQuestion.setText(
                        numero + ". " + question.getLibelle()
                );

                texteQuestion.setBold(true);
                texteQuestion.setFontFamily("Arial");
                texteQuestion.setFontSize(12);
                texteQuestion.setColor("164E63");

                // Libellé Réponse
                XWPFParagraph paragrapheReponse =
                        document.createParagraph();

                paragrapheReponse.setSpacingAfter(70);
                paragrapheReponse.setKeepNext(true);

                XWPFRun libelleReponse =
                        paragrapheReponse.createRun();

                libelleReponse.setText("Réponse");
                libelleReponse.setBold(true);
                libelleReponse.setFontFamily("Arial");
                libelleReponse.setFontSize(10);
                libelleReponse.setColor("047857");

                // Texte complet de la réponse
                ajouterTexteReponse(
                        document,
                        reponse.getReponse()
                );

                numero++;
            }

            // Pied de page
            XWPFFooter footer =
                    document.createFooter(HeaderFooterType.DEFAULT);

            XWPFParagraph paragraphePied =
                    footer.createParagraph();

            paragraphePied.setAlignment(
                    ParagraphAlignment.CENTER
            );

            XWPFRun textePied = paragraphePied.createRun();

            textePied.setText(
                    "Plateforme Entrepreneur360"
            );

            textePied.setFontFamily("Arial");
            textePied.setFontSize(8);
            textePied.setColor("64748B");

            // Générer les octets du fichier DOCX
            document.write(output);

            return output.toByteArray();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Une erreur est survenue lors de la génération du document Word.",
                    e
            );
        }
    }


    // Ajouter une ligne d'information avec une présentation homogène
    private void ajouterLigneInformation(
            XWPFDocument document,
            String libelle,
            String valeur) {

        XWPFParagraph paragraphe = document.createParagraph();

        paragraphe.setSpacingAfter(90);

        XWPFRun texteLibelle = paragraphe.createRun();

        texteLibelle.setText(libelle + " : ");
        texteLibelle.setBold(true);
        texteLibelle.setFontFamily("Arial");
        texteLibelle.setFontSize(10);
        texteLibelle.setColor("334155");

        XWPFRun texteValeur = paragraphe.createRun();

        texteValeur.setText(
                valeur == null ? "Non renseigné" : valeur
        );

        texteValeur.setFontFamily("Arial");
        texteValeur.setFontSize(10);
        texteValeur.setColor("1E293B");
    }


    // Préserver les paragraphes et les listes des réponses longues
    private void ajouterTexteReponse(
            XWPFDocument document,
            String texte) {

        if (texte == null || texte.isBlank()) {
            XWPFParagraph paragraphe = document.createParagraph();

            XWPFRun run = paragraphe.createRun();

            run.setText("Aucune réponse renseignée.");
            run.setItalic(true);
            run.setFontFamily("Arial");
            run.setFontSize(11);

            return;
        }

        String texteNormalise = texte
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        String[] lignes = texteNormalise.split("\n", -1);

        for (String ligne : lignes) {

            XWPFParagraph paragraphe = document.createParagraph();

            paragraphe.setAlignment(ParagraphAlignment.LEFT);
            paragraphe.setSpacingAfter(100);
            paragraphe.setSpacingBetween(1.15);

            String contenu = ligne.strip();

            if (contenu.isEmpty()) {
                paragraphe.setSpacingAfter(60);
                continue;
            }

            // Indentation légère des éléments de liste
            if (contenu.startsWith("•")
                    || contenu.startsWith("-")
                    || contenu.startsWith("–")) {

                paragraphe.setIndentationLeft(360);
            }

            XWPFRun run = paragraphe.createRun();

            run.setText(contenu);
            run.setFontFamily("Arial");
            run.setFontSize(11);
            run.setColor("1E293B");
        }
    }



}
