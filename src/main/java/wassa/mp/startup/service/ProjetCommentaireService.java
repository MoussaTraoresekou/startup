package wassa.mp.startup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wassa.mp.startup.dto.CommentaireResponseDto;
import wassa.mp.startup.model.Investisseur;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetCommentaire;
import wassa.mp.startup.repository.InvestisseurRepository;
import wassa.mp.startup.repository.ProjetCommentaireRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjetCommentaireService {

    private final ProjetCommentaireRepository commentaireRepository;
    private final ProjetRepository projetRepository;
    private final InvestisseurRepository investisseurRepository;

    @Transactional
    public CommentaireResponseDto ajouter(String email, int projetId, String contenu) {
        Investisseur investisseur = trouverInvestisseur(email);

        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable"));

        ProjetCommentaire commentaire = new ProjetCommentaire();
        commentaire.setContenu(contenu.trim());
        commentaire.setInvestisseur(investisseur);
        commentaire.setProjet(projet);

        return versDto(commentaireRepository.save(commentaire), investisseur);
    }

    @Transactional(readOnly = true)
    public List<CommentaireResponseDto> lister(String email, int projetId) {
        Investisseur courant = trouverInvestisseur(email);

        return commentaireRepository.findByProjetIdOrderByDateCreationDesc(projetId)
                .stream()
                .map(commentaire -> versDto(commentaire, courant))
                .toList();
    }

    private CommentaireResponseDto versDto(ProjetCommentaire commentaire, Investisseur courant) {
        Investisseur auteur = commentaire.getInvestisseur();

        return new CommentaireResponseDto(
                commentaire.getId(),
                commentaire.getContenu(),
                nomComplet(auteur),
                commentaire.getDateCreation(),
                Objects.equals(auteur.getId(), courant.getId())
        );
    }
    private String nomComplet(Investisseur investisseur) {
        return (Objects.toString(investisseur.getPrenom(), "") + " "
                + Objects.toString(investisseur.getNom(), "")).trim();
    }

    private Investisseur trouverInvestisseur(String email) {
        return investisseurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Investisseur introuvable"));
    }
}