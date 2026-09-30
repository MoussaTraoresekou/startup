package wassa.mp.startup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wassa.mp.startup.model.Porteur;

public interface PorteurRepository extends JpaRepository<Porteur, Integer> {

}
