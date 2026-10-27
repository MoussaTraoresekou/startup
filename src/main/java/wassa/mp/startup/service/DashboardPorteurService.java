package wassa.mp.startup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.StatutEtapeEnum;
import wassa.mp.startup.Enumeration.StatutMembre;
import wassa.mp.startup.dto.DashboardPorteurDto;
import wassa.mp.startup.exception.NonAutoriseException;
import wassa.mp.startup.exception.ResourceNotFoundException;
import wassa.mp.startup.model.MembreProjet;
import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.ProjetEtape;
import wassa.mp.startup.repository.MembreProjetRepository;
import wassa.mp.startup.repository.ProjetEtapeRepository;
import wassa.mp.startup.repository.ProjetLikeRepository;
import wassa.mp.startup.repository.ProjetRepository;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DashboardPorteurService {

    private final ProjetRepository projetRepository;
    private final ProjetEtapeRepository projetEtapeRepository;
    private final ProjetLikeRepository projetLikeRepository;
    private final MembreProjetRepository membreProjetRepository;

    @Transactional(readOnly = true)
    public DashboardPorteurDto obtenirDashboard(
            int projetId,
            CustumUserDetail userConnecter) {

        // 1. Récupérer le projet
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Le projet demandé n'existe pas."
                        )
                );

        // 2. Vérifier que le projet appartient au porteur connecté
        if (projet.getPorteur() == null
                || projet.getPorteur().getId()
                != userConnecter.getUser().getId()) {

            throw new NonAutoriseException(
                    "Vous n'êtes pas autorisé à consulter ce projet."
            );
        }

        // 3. Récupérer toutes les étapes enregistrées pour ce projet
        List<ProjetEtape> etapes =
                projetEtapeRepository.findByProjetId(projetId);

        // 4. Calculer les indicateurs à partir des vrais statuts
        long etapesValidees = etapes.stream()
                .filter(e ->
                        e.getStatutEtape() == StatutEtapeEnum.VALIDE
                )
                .count();

        long etapesEnCours = etapes.stream()
                .filter(e ->
                        e.getStatutEtape() == StatutEtapeEnum.EN_COUR
                )
                .count();

        // 5. Compter les likes et récupérer les membres actifs
        long investisseursInteresses =
                projetLikeRepository.countByProjetId(projetId);

        List<MembreProjet> membres =
                membreProjetRepository.findByProjetIdAndStatut(
                        projetId,
                        StatutMembre.ACTIF
                );

        // 6. Transformer les étapes en DTO
        List<DashboardPorteurDto.EtapeDto> etapesDto =
                etapes.stream()
                        .map(e -> new DashboardPorteurDto.EtapeDto(
                                e.getId(),
                                e.getEtape() != null
                                        ? String.valueOf(e.getEtape().getType())
                                        : "Étape non renseignée",
                                e.getStatutEtape() != null
                                        ? e.getStatutEtape().name()
                                        : null,
                                e.getDateSoumission(),
                                e.getDateValidation(),
                                e.getCommentaireMentor()
                        ))
                        .toList();

        // 7. Transformer les membres actifs en DTO
        List<DashboardPorteurDto.MembreDto> equipeDto =
                membres.stream()
                        .map(m -> new DashboardPorteurDto.MembreDto(
                                m.getUser().getId(),
                                m.getUser().getNom(),
                                m.getUser().getPrenom(),
                                m.getRoleProjet() != null
                                        ? m.getRoleProjet().name()
                                        : null
                        ))
                        .toList();

        // 8. Trouver le dernier commentaire de mentor disponible
        ProjetEtape derniereEtapeCommentee = etapes.stream()
                .filter(e ->
                        e.getCommentaireMentor() != null
                                && !e.getCommentaireMentor().isBlank()
                )
                .max(
                        Comparator.comparing(
                                this::dateReference,
                                Comparator.nullsFirst(
                                        Comparator.<LocalDate>naturalOrder()
                                )
                        )
                )
                .orElse(null);

        DashboardPorteurDto.RetourMentorDto retourMentor = null;

        if (derniereEtapeCommentee != null) {
            retourMentor = new DashboardPorteurDto.RetourMentorDto(
                    derniereEtapeCommentee.getCommentaireMentor(),
                    derniereEtapeCommentee.getEtape() != null
                            ? String.valueOf(
                            derniereEtapeCommentee
                                    .getEtape()
                                    .getType()
                    )
                            : null,
                    dateReference(derniereEtapeCommentee)
            );
        }

        // 9. Construire la réponse du dashboard
        return new DashboardPorteurDto(
                projet.getId(),
                projet.getTitre(),
                investisseursInteresses,
                membres.size(),
                etapesValidees,
                etapesEnCours,
                etapesDto,
                equipeDto,
                retourMentor
        );
    }

    private LocalDate dateReference(ProjetEtape etape) {
        if (etape.getDateValidation() != null) {
            return etape.getDateValidation();
        }

        return etape.getDateSoumission();
    }
}