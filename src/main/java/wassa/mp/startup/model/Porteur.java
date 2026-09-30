package wassa.mp.startup.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@AllArgsConstructor @NoArgsConstructor @Data
public class Porteur extends User{
    @OneToMany(mappedBy = "porteur")
    private List<Projet> projets;
}
