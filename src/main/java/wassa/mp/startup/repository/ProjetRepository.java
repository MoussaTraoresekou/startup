package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wassa.mp.startup.model.Projet;
import wassa.mp.startup.model.SecteurActivite;

import java.util.List;
import java.util.Set;

public interface ProjetRepository extends JpaRepository<Projet, Integer> {
    @Query("SELECT p from Projet p where p.porteur.id=:porteur_id order by p.id desc")
    List<Projet> findByPorteur(@Param("porteur_id") int porteur_id);


    long countByPorteurId(int id);
/*
    List<Projet> findBySecteurActiviteIn(List<Integer> secteurIds);
    List<Projet> findBySecteurActiviteIdIn(List<Integer> secteurIds);

 */
@Query("SELECT p FROM Projet p WHERE p.secteurActivite.id IN :secteurIds")
List<Projet> findBySecteurActiviteIdIn(@Param("secteurIds") List<Integer> secteurIds);
}
