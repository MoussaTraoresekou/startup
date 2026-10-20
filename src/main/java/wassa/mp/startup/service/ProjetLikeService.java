package wassa.mp.startup.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wassa.mp.startup.model.Investisseur;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetLike;
import wassa.mp.startup.repository.InvestisseurRepository;
import wassa.mp.startup.repository.ProjetLikeRepository;
import wassa.mp.startup.repository.ProjetRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetLikeService {

    private final ProjetLikeRepository likeRepository;
    private final ProjetRepository projetRepository;
    private final InvestisseurRepository investisseurRepository;

    /** true si le projet est maintenant aimé, false si le like a été retiré. */
    @Transactional
    public boolean basculer(String email, int projetId) {
        Investisseur investisseur = trouverInvestisseur(email);

        if (likeRepository.existsByInvestisseurIdAndProjetId(investisseur.getId(), projetId)) {
            likeRepository.deleteByInvestisseurIdAndProjetId(investisseur.getId(), projetId);
            return false;
        }

        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable"));

        ProjetLike like = new ProjetLike();
        like.setInvestisseur(investisseur);
        like.setProjet(projet);
        likeRepository.save(like);
        return true;
    }

    public List<Integer> projetsLikes(String email) {
        return likeRepository.findProjetIdsByInvestisseur(trouverInvestisseur(email).getId());
    }

    public long nombreLikes(int projetId) {
        return likeRepository.countByProjetId(projetId);
    }

    private Investisseur trouverInvestisseur(String email) {
        return investisseurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Investisseur introuvable"));
    }
}