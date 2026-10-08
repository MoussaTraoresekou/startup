package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import wassa.mp.startup.dto.ProjetEtapeResponseDto;
import wassa.mp.startup.dto.ProjetEtapesRequestDto;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.Etape;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.model.Reponse;
import wassa.mp.startup.repository.EtapeRepository;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.nio.charset.StandardCharsets;
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
}
