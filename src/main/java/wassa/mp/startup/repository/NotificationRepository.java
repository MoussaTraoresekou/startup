package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wassa.mp.startup.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    @Query("Select n from Notification n where n.user.id=:utilisateurId")
    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(@Param("utilisateurId") int utilisateurId);

}
