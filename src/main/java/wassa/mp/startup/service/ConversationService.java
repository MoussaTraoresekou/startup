    package wassa.mp.startup.service;
    import org.springframework.security.access.AccessDeniedException;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import wassa.mp.startup.CustumUserDetail;
    import wassa.mp.startup.dto.ConversationDTO;
    import wassa.mp.startup.exception.NonAutoriseException;
    import wassa.mp.startup.exception.ResourceNotFoundException;
    import wassa.mp.startup.model.*;
    import wassa.mp.startup.repository.ConversationRepository;
    import wassa.mp.startup.repository.MessageRepository;
    import wassa.mp.startup.repository.ProjetRepository;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ProjetRepository projetRepository;

    public ConversationService(
    ConversationRepository conversationRepository,
    MessageRepository messageRepository,
    ProjetRepository projetRepository) {

    this.conversationRepository = conversationRepository;
    this.messageRepository = messageRepository;
    this.projetRepository = projetRepository;
    }


    @Transactional
    public ConversationDTO ouvrirConversation(int projetId,  CustumUserDetail userConnecter) {

    if (!(userConnecter.getUser() instanceof Investisseur investisseur)) {
    throw new AccessDeniedException(
        "Seul un investisseur peut initier une conversation");
    }

    Projet projet = projetRepository.findById(projetId)
    .orElseThrow(() ->
            new IllegalArgumentException("Projet introuvable"));

    Porteur porteur = projet.getPorteur();

    if (porteur == null) {
    throw new IllegalStateException(
        "Ce projet n'a pas de porteur associé");
    }

    Conversation conversation = conversationRepository.findByInvestisseurIdAndProjetId(investisseur.getId(), projet.getId())
            .orElseGet(() -> {
                Conversation nouvelle = new Conversation();

                nouvelle.setInvestisseur(investisseur);
                nouvelle.setPorteur(porteur);
                nouvelle.setProjet(projet);
                nouvelle.setCreatedAt(LocalDateTime.now());

                return conversationRepository.save(nouvelle);
            });

    return convertirEnDTO(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> mesConversations( CustumUserDetail userConnecter) {

    List<Conversation> conversations;

    if (userConnecter.getUser() instanceof Investisseur investisseur) {
    conversations =
        conversationRepository
                .findByInvestisseurIdOrderByLastMessageAtDesc(
                        investisseur.getId());

    } else if (userConnecter.getUser() instanceof Porteur porteur) {
    conversations =
        conversationRepository
                .findByPorteurIdOrderByLastMessageAtDesc(
                        porteur.getId());

    } else {
    throw new AccessDeniedException(
        "La messagerie privée est réservée aux investisseurs "
                + "et aux porteurs de projet");
    }

    return conversations.stream()
    .map(this::convertirEnDTO)
    .toList();
    }

        @Transactional(readOnly = true)
        public Conversation verifierAcces(
                int conversationId,
                CustumUserDetail userConnecte) {

            Conversation conversation = conversationRepository
                    .findById(conversationId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Conversation introuvable"));

            if (userConnecte == null || userConnecte.getUser() == null) {
                throw new NonAutoriseException(
                        "Utilisateur non authentifié");
            }

            int userId = userConnecte.getUser().getId();

            boolean estInvestisseur =
                    conversation.getInvestisseur().getId() == userId;

            boolean estPorteur =
                    conversation.getPorteur().getId() == userId;

            if (!estInvestisseur && !estPorteur) {
                throw new NonAutoriseException(
                        "Tu ne participes pas à cette conversation");
            }

            return conversation;
        }

    private ConversationDTO convertirEnDTO(
    Conversation conversation) {
    ConversationDTO dto = new ConversationDTO();
    dto.setId(conversation.getId());
    dto.setProjetId(conversation.getProjet().getId());
    dto.setProjetTitre(conversation.getProjet().getTitre());
    dto.setInvestisseurId(
    conversation.getInvestisseur().getId());
    dto.setPorteurId(
    conversation.getPorteur().getId());

    dto.setInvestisseurNom(
    conversation.getInvestisseur().getNomComplet());
    dto.setPorteurNom(
    conversation.getPorteur().getNomComplet());

    dto.setDateCreation(conversation.getCreatedAt());
    dto.setDateDernierMessage(conversation.getLastMessageAt());

    messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
    .ifPresent(message ->
            dto.setDernierMessage(message.getContenu()));

    return dto;
    }
    }
