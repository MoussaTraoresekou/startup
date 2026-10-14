package wassa.mp.startup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.EnumCandidature;
import wassa.mp.startup.dto.CandidatureRequestDto;
import wassa.mp.startup.dto.CandidatureResponseDto;
import wassa.mp.startup.dto.OffresRequestDto;
import wassa.mp.startup.dto.OffresResponseDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.OperationInterditeException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.*;
import wassa.mp.startup.Enumeration.EnumOffre;
import wassa.mp.startup.repository.CandidatureRepository;
import wassa.mp.startup.repository.CollaborateurRepository;
import wassa.mp.startup.repository.OffresRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OffresService {

    @Autowired
    private OffresRepository offresRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private CollaborateurRepository collaborateurRepository;

    @Transactional
    public OffresResponseDto publierAppelCandidature(OffresRequestDto dto, CustumUserDetail userConnecter) {
        Projet projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Le projet spécifié n'existe pas."));

        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas le propriétaire de ce projet.");
        }

        Offres offre = new Offres();
        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setProfilRecherche(dto.getProfilRecherche());
        offre.setDatePublication(LocalDateTime.now());
        offre.setStatuOffre(EnumOffre.OUVERTE);
        offre.setProjet(projet);

        Offres saved = offresRepository.save(offre);
        return mapToOffresResponseDto(saved);
    }

    @Transactional
    public CandidatureResponseDto postulerAUneOffre(CandidatureRequestDto dto, CustumUserDetail userConnecter) {
        Offres offre = offresRepository.findById(dto.getOffresId())
                .orElseThrow(() -> new ResourceNotFoundException("L'annonce spécifiée n'existe pas."));

        if (offre.getStatuOffre() == EnumOffre.CLOTUREE) {
            throw new OperationInterditeException("Cette offre est déjà clôturée.");
        }

        Collaborateur collab = collaborateurRepository.findById(userConnecter.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil collaborateur introuvable."));

        Candidature candidature = new Candidature();
        candidature.setMotivation(dto.getMotivation());
        candidature.setStatuCandidature(EnumCandidature.EN_ATTENTE);
        candidature.setDateCreation(LocalDateTime.now());
        candidature.setCollaborateur(collab);
        candidature.setOffres(offre);

        Candidature saved = candidatureRepository.save(candidature);
        return mapToCandidatureResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<OffresResponseDto> obtenirToutesLesOffresOuvertes() {
        return offresRepository.findByStatuOffre(EnumOffre.OUVERTE).stream()
                .map(this::mapToOffresResponseDto)
                .toList();
    }

    /*
    @Transactional(readOnly = true)
    public List<CandidatureResponseDto> obtenirCandidaturesPourUneOffre(int offresId, CustumUserDetail userConnecter) {
        Offres offre = offresRepository.findById(offresId)
                .orElseThrow(() -> new ResourceNotFoundException("L'annonce spécifiée n'existe pas."));

        if (offre.getProjet().getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Accès refusé : vous n'êtes pas le porteur de ce projet.");
        }

        return candidatureRepository.findByOffresId(offresId).stream()
                .map(this::mapToCandidatureResponseDto)
                .toList();
    }

     */
    @Transactional(readOnly = true)
    public List<CandidatureResponseDto> obtenirToutesLesCandidaturesDuProjet(int projetId, CustumUserDetail userConnecter) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Le projet spécifié n'existe pas."));

        // Sécurité : On vérifie que l'utilisateur connecté est bien le fondateur de ce projet
        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Accès refusé : vous n'êtes pas le porteur de ce projet.");
        }

        //  On extrait TOUTES les candidatures de TOUTES les offres de ce projet d'un coup !
        return candidatureRepository.findByOffresProjetId(projetId).stream()
                .map(this::mapToCandidatureResponseDto)
                .toList();
    }


    private OffresResponseDto mapToOffresResponseDto(Offres offre) {
        return new OffresResponseDto(
                offre.getId(),
                offre.getTitre(),
                offre.getDescription(),
                offre.getProfilRecherche(),
                offre.getDatePublication(),
                offre.getStatuOffre(),
                offre.getProjet().getId(),
                offre.getProjet().getTitre(),
                offre.getProjet().getPorteur().getPrenom(),
                offre.getProjet().getPorteur().getNom()
        );
    }

    private CandidatureResponseDto mapToCandidatureResponseDto(Candidature candidature) {
        return new CandidatureResponseDto(
                candidature.getId(),
                candidature.getStatuCandidature(),
                candidature.getMotivation(),
                candidature.getDateCreation(),
                candidature.getCollaborateur().getId(),
                candidature.getCollaborateur().getNom(),
                candidature.getCollaborateur().getPrenom(),
                candidature.getCollaborateur().getEmail(),
                candidature.getCollaborateur().getTelephone(),
                candidature.getCollaborateur().getSpecialite(),
                candidature.getCollaborateur().getBiographie(),
                candidature.getCollaborateur().getLienPortfolio(),
                candidature.getOffres().getId(),
                candidature.getOffres().getTitre()
        );
    }
}
