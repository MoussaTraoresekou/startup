package wassa.mp.startup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import wassa.mp.startup.CustumUserDetail;
import wassa.mp.startup.Enumeration.RoleEnum;
import wassa.mp.startup.dto.InvestorRegisterDTO;
import wassa.mp.startup.dto.InvestorResponseDto;
import wassa.mp.startup.dto.ProfilResponse;
import wassa.mp.startup.dto.SecteurActiviteResponseDto;
import wassa.mp.startup.model.Investisseur;
import wassa.mp.startup.model.SecteurActivite;
import wassa.mp.startup.repository.InvestisseurRepository;
import wassa.mp.startup.repository.SecteurActiviteRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InvestisseurService {

    private final InvestisseurRepository investisseurRepository;
    private final SecteurActiviteRepository secteurActiviteRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private  final EmailVerificationService emailVerificationService;
    @Transactional(readOnly = true)
    public List<SecteurActiviteResponseDto> listerSecteurs() {
        return secteurActiviteRepository.findAll().stream()
                .sorted(Comparator.comparing(SecteurActivite::getNom,
                        String.CASE_INSENSITIVE_ORDER))
                .map(s -> new SecteurActiviteResponseDto(s.getId(), s.getNom()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfilResponse getProfil(String email) {
        return toResponse(trouver(email));
    }

    @Transactional
    public ProfilResponse enregistrerSecteurs(String email, List<Integer> secteurIds) {
        Investisseur investisseur = trouver(email);

        Set<Integer> idsDemandes = new HashSet<>(secteurIds);
        List<SecteurActivite> secteurs =
                secteurActiviteRepository.findAllById(idsDemandes);

        if (secteurs.size() != idsDemandes.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Un ou plusieurs secteurs sont inconnus");
        }

        investisseur.setSecteursInteret(new HashSet<>(secteurs));
        investisseur.setProfileCompleted(true);
        investisseurRepository.save(investisseur);
        return toResponse(investisseur);
    }

    /**
     * L'investisseur clique sur « Passer » : on ne lui redemande plus
     * à chaque connexion. Ses secteurs restent vides ; le dashboard
     * pourra afficher un rappel tant que la liste est vide.
     */
    @Transactional
    public ProfilResponse passer(String email) {
        Investisseur investisseur = trouver(email);
        investisseur.setProfileCompleted(true);
        investisseurRepository.save(investisseur);
        return toResponse(investisseur);
    }

    private Investisseur trouver(String email) {
        return investisseurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Investisseur introuvable"));
    }

    private ProfilResponse toResponse(Investisseur investisseur) {
        List<Integer> ids = investisseur.getSecteursInteret().stream()
                .map(SecteurActivite::getId)
                .sorted()
                .toList();
        return new ProfilResponse(investisseur.isProfileCompleted(), ids);
    }
    @Transactional
    public InvestorResponseDto registerInvestisseur(
            InvestorRegisterDTO investorRegisterDto) {

        Investisseur investisseur = new Investisseur();

        // Hydratation des propriétés héritées de la classe mère User
        investisseur.setNom(investorRegisterDto.getNom());
        investisseur.setPrenom(investorRegisterDto.getPrenom());
        investisseur.setEmail(investorRegisterDto.getEmail().trim().toLowerCase());

        investisseur.setPassword(
                bCryptPasswordEncoder.encode(
                        investorRegisterDto.getPassword()
                )
        );

        investisseur.setTelephone(investorRegisterDto.getTelephone());
        investisseur.setDateCreation(LocalDate.now());
        investisseur.setRole(RoleEnum.INVESTISSEUR);

        // Hydratation des propriétés spécifiques à la table Investisseur
        investisseur.setTypeInvestisseur(investorRegisterDto.getTypeInvestisseur());
        investisseur.setProfileCompleted(false);

        // Persistance en base de données (stratégie JOINED héritée)
        Investisseur pourAjouter = investisseurRepository.save(investisseur);

        // Déclenchement automatique de l'envoi du lien de vérification par e-mail
        emailVerificationService.envoyerLienVerification(pourAjouter);

        // Retour du DTO de réponse structuré
        return new InvestorResponseDto(
                pourAjouter.getId(),
                pourAjouter.getNom(),
                pourAjouter.getPrenom(),
                pourAjouter.getTelephone(),
                pourAjouter.getEmail(),
                pourAjouter.getTypeInvestisseur(),
                pourAjouter.isProfileCompleted()
        );
    }
    public List<SecteurActiviteResponseDto>getAllSecteurActivitesChoisi(CustumUserDetail userConnecter) {
        return investisseurRepository.getAllSecteurChoisi(userConnecter.getUser().getId()).stream().map(
                 secteurActivite->new SecteurActiviteResponseDto(secteurActivite.getId(),secteurActivite.getNom())
        ).toList();

    }

}