package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wassa.mp.startup.model.Investisseur;
import wassa.mp.startup.model.SecteurActivite;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestisseurRepository extends JpaRepository<Investisseur, Integer> {

    /** Utilisé par le profil : l'e-mail vient du token de l'utilisateur connecté. */
    Optional<Investisseur> findByEmail(String email);

    /** Utile pour refuser une inscription avec un e-mail déjà utilisé. */
    boolean existsByEmail(String email);

    @Query("""
    SELECT s
    FROM Investisseur i
    JOIN i.secteursInteret s
    WHERE i.id = :id
""")
    List<SecteurActivite> getAllSecteurChoisi(@Param("id") int id);

}