package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import wassa.mp.startup.model.Projet;

import java.util.List;

public interface ProjetRepository extends JpaRepository<Projet, Integer> {
    @Query("SELECT p from Projet p where p.porteur.id=:porteur_id")
    List<Projet> findByPorteur(@Param("porteur_id") int porteur_id);


}
