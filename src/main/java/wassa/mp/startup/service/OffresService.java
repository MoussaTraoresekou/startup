    package wassa.mp.startup.service;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import wassa.mp.startup.CustumUserDetail;
    import wassa.mp.startup.Enumeration.EnumCandidature;
    import wassa.mp.startup.dto.*;
    import wassa.mp.startup.exception.NonAutoriseException;
    import wassa.mp.startup.exception.OperationInterditeException;
    import wassa.mp.startup.exception.ResourceNotFoundException;
    import wassa.mp.startup.model.*;
    import wassa.mp.startup.Enumeration.EnumOffre;
    import wassa.mp.startup.repository.*;

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
    private PorteurRepository porteurRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SpecialiteRepository specialiteRepository;
    /*
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
    public OffresResponseDto publierAppelCandidature(OffresRequestDto dto, CustumUserDetail userConnecter) {
    Projet projet = projetRepository.findById(dto.getProjetId())
            .orElseThrow(() -> new ResourceNotFoundException("Le projet spécifié n'existe pas."));
    Specialite specialite = specialiteRepository.findById(dto.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("La sepecialité spécifiée n'existe pas."));


        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
        throw new NonAutoriseException("Vous n'êtes pas le propriétaire de ce projet.");
    }

    Offres offre = new Offres();
    offre.setTitre(dto.getTitre());
    offre.setDescription(dto.getDescription());
    offre.setProfilRecherche(dto.getProfilRecherche());
    offre.setDatePublication(LocalDateTime.now());
    offre.setStatuOffre(EnumOffre.OUVERTE);
    offre.setSpecialite(specialite);
    offre.setProjet(projet);

    Offres saved = offresRepository.save(offre);

    List<Porteur> autresPorteurs = porteurRepository.findByIdNot(userConnecter.getUser().getId());

    String msgAlerte = "Nouvelle opportunité : Le projet " + projet.getTitre() + " recherche un " + saved.getTitre();
    AlerteOffreDto alerteDto = new AlerteOffreDto(
            saved.getId(),
            projet.getId(),
            projet.getTitre(),
            msgAlerte,
            LocalDateTime.now()
    );

    for (Porteur destinataire : autresPorteurs) {
        notificationService.notifierUtilisateur(destinataire, alerteDto);
    }

    return mapToOffresResponseDto(saved);
    }

    /*
    @Transactional
    public CandidatureResponseDto postulerAUneOffre(CandidatureRequestDto dto, CustumUserDetail userConnecter) {
        Offres offre = offresRepository.findById(dto.getOffresId())
                .orElseThrow(() -> new ResourceNotFoundException("L'annonce spécifiée n'existe pas."));

        if (offre.getStatuOffre() == EnumOffre.CLOTUREE) {
            throw new OperationInterditeException("Cette offre est déjà clôturée.");
        }

        Porteur porteur = porteurRepository.findById(userConnecter.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("compte n'existe  pas."));

        Candidature candidature = new Candidature();
        candidature.setMotivation(dto.getMotivation());
        candidature.setStatuCandidature(EnumCandidature.EN_ATTENTE);
        candidature.setDateCreation(LocalDateTime.now());
        candidature.setPorteur(porteur);
        candidature.setOffres(offre);

        Candidature saved = candidatureRepository.save(candidature);
        return mapToCandidatureResponseDto(saved);
    }

    */
    @Transactional(readOnly = true)
    public List<OffresResponseDto> obtenirToutesMesOffres() {
        return offresRepository.findByStatuOffre(EnumOffre.OUVERTE).stream()
                .map(this::mapToOffresResponseDto)
                .toList();
    }

    /*
    @Transactional
    public CandidatureResponseDto postulerAUneOffre(CandidatureRequestDto dto, CustumUserDetail userConnecter) {
    Offres offre = offresRepository.findById(dto.getOffresId())
            .orElseThrow(() -> new ResourceNotFoundException("L'annonce spécifiée n'existe pas."));

    if (offre.getStatuOffre() == EnumOffre.CLOTUREE) {
        throw new OperationInterditeException("Cette offre est déjà clôturée.");
    }

    Porteur porteur = porteurRepository.findById(userConnecter.getUser().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Compte n'existe pas."));

    if (offre.getProjet().getPorteur().getId() == porteur.getId()) {
        throw new OperationInterditeException("Vous ne pouvez pas postuler à votre propre offre.");
    }

    Candidature candidature = new Candidature();
    candidature.setMotivation(dto.getMotivation());
    candidature.setStatuCandidature(EnumCandidature.EN_ATTENTE);
    candidature.setDateCreation(LocalDateTime.now());
    candidature.setPorteur(porteur);
    candidature.setOffres(offre);

    Candidature saved = candidatureRepository.save(candidature);

    Porteur proprietaireProjet = offre.getProjet().getPorteur();

    String msgAlerte = porteur.getPrenom() + " " + porteur.getNom() + " a postulé pour l'offre : " + offre.getTitre();
    AlerteCandidature alerteDto = new AlerteCandidature(
            saved.getId(),
            offre.getProjet().getId(),
            offre.getProjet().getTitre(),
            porteur.getId(),
            porteur.getNomComplet(),
            msgAlerte,
            LocalDateTime.now()
    );

    notificationService.notifierUtilisateur(proprietaireProjet, alerteDto);

    return mapToCandidatureResponseDto(saved);
    }

    */
    @Transactional
    public OffresResponseDto publierAppelCandidature(OffresRequestDto dto, CustumUserDetail userConnecter) {
        Projet projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Le projet spécifié n'existe pas."));

        Specialite specialite = specialiteRepository.findById(dto.getSpecialite())
                .orElseThrow(() -> new ResourceNotFoundException("La spécialité spécifiée n'existe pas."));

        if (projet.getPorteur().getId() != userConnecter.getUser().getId()) {
            throw new NonAutoriseException("Vous n'êtes pas le propriétaire de ce projet.");
        }

        Offres offre = new Offres();
        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setProfilRecherche(dto.getProfilRecherche());
        offre.setDatePublication(LocalDateTime.now());
        offre.setStatuOffre(EnumOffre.OUVERTE);
        offre.setSpecialite(specialite);
        offre.setProjet(projet);

        Offres saved = offresRepository.save(offre);

        List<Porteur> porteursCibles = porteurRepository.findBySpecialiteIdAndIdNot(specialite.getId(), userConnecter.getUser().getId());

        String msgAlerte = "Nouvelle opportunité : Le projet " + projet.getTitre() + " recherche un " + saved.getTitre();
        AlerteOffreDto alerteDto = new AlerteOffreDto(
                saved.getId(),
                projet.getId(),
                projet.getTitre(),
                msgAlerte,
                LocalDateTime.now()
        );

        for (Porteur destinataire : porteursCibles) {
            notificationService.notifierUtilisateur(destinataire, alerteDto);
        }

        return mapToOffresResponseDto(saved);
    }

    @Transactional
    public CandidatureResponseDto postulerAUneOffre(CandidatureRequestDto dto, CustumUserDetail userConnecter) {
    Offres offre = offresRepository.findById(dto.getOffresId())
            .orElseThrow(() -> new ResourceNotFoundException("L'annonce spécifiée n'existe pas."));
    if (offre.getStatuOffre() == EnumOffre.CLOTUREE) {
        throw new OperationInterditeException("Cette offre est déjà clôturée.");
    }

    Porteur porteur = porteurRepository.findById(userConnecter.getUser().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Compte n'existe pas."));

    // BARRIÈRE DE SÉCURITÉ : Interdit la double postulation
    if (candidatureRepository.existsByPorteurIdAndOffresId(porteur.getId(), offre.getId())) {
        throw new OperationInterditeException("Vous avez déjà postulé à cet appel à candidature.");
    }

    if (offre.getProjet().getPorteur().getId() == porteur.getId()) {
        throw new OperationInterditeException("Vous ne pouvez pas postuler à votre propre offre.");
    }

    Candidature candidature = new Candidature();
    candidature.setMotivation(dto.getMotivation());
    candidature.setStatuCandidature(EnumCandidature.EN_ATTENTE);
    candidature.setDateCreation(LocalDateTime.now());
    candidature.setPorteur(porteur);
    candidature.setOffres(offre);

    Candidature saved = candidatureRepository.save(candidature);

    Porteur proprietaireProjet = offre.getProjet().getPorteur();

    String msgAlerte = porteur.getPrenom() + " " + porteur.getNom() + " a postulé pour l'offre : " + offre.getTitre();
    AlerteCandidature alerteDto = new AlerteCandidature(
            saved.getId(),
            offre.getProjet().getId(),
            offre.getProjet().getTitre(),
            porteur.getId(),
            porteur.getNomComplet(),
            msgAlerte,
            LocalDateTime.now()
    );

    notificationService.notifierUtilisateur(proprietaireProjet, alerteDto);
    return mapToCandidatureResponseDto(saved);
    }
    /*
    @Transactional(readOnly = true)
    public List<OffresResponseDto> obtenirToutesLesOffresOuvertes(CustumUserDetail userConnecter) {
        int idUtilisateurConnecte = userConnecter.getUser().getId(); // Récupère l'ID de l'utilisateur connecté

        return offresRepository.findByStatuOffre(EnumOffre.OUVERTE).stream()
                // 💡 On filtre : on ne garde que les offres dont l'auteur N'EST PAS l'utilisateur connecté
                .filter(offre -> offre.getProjet().getPorteur().getId() != idUtilisateurConnecte)
                .map(this::mapToOffresResponseDto)
                .toList();
    }

    */
    @Transactional(readOnly = true)
    public List<OffresResponseDto> obtenirToutesLesOffresOuvertes(CustumUserDetail userConnecter) {
    Porteur porteurActuel = (Porteur) userConnecter.getUser();
    if (porteurActuel.getSpecialite() == null) {
        return java.util.Collections.emptyList();
    }

    return offresRepository.findMatchesPourLePorteur(EnumOffre.OUVERTE, porteurActuel.getId(), porteurActuel.getSpecialite().getId()).stream()
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
        return candidatureRepository.findByOffresProjetIdAndStatuCandidatureOrderByIdDesc(projetId,EnumCandidature.EN_ATTENTE).stream()
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
                offre.getProjet().getPorteur().getNom(),
                offre.getSpecialite().getLibelle()

        );
    }
    private CandidatureResponseDto mapToCandidatureResponseDto(Candidature candidature) {
        Porteur candidat = candidature.getPorteur(); // C'est un Porteur qui joue le rôle de collaborateur

        return new CandidatureResponseDto(
                candidature.getId(),
                candidature.getStatuCandidature(),
                candidature.getMotivation(),
                candidature.getDateCreation(),
                candidat.getId(),
                candidat.getNom(),
                candidat.getPrenom(),
                candidat.getEmail(),
                candidat.getTelephone(),
                candidat.getSpecialite() != null ? candidat.getSpecialite().getLibelle() : "Aucune",
                candidat.getBiographie(), // Ajusté suite au correctif de la faute de frappe
                candidat.getLienPortfolio(),
                candidature.getOffres().getId(),
                candidature.getOffres().getTitre()
        );
    }

    }
