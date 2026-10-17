package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.model.Investisseur;

import java.util.Optional;

@Repository
public interface InvestisseurRepository extends JpaRepository<Investisseur, Integer> {

    /** Utilisé par le profil : l'e-mail vient du token de l'utilisateur connecté. */
    Optional<Investisseur> findByEmail(String email);

    /** Utile pour refuser une inscription avec un e-mail déjà utilisé. */
    boolean existsByEmail(String email);
}