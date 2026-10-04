package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wassa.mp.startup.Enumeration.RoleEnum;

import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor @NoArgsConstructor @Data
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String prenom;
    @Column(unique = true,nullable = false)
    private String password;
    @Column(unique = true,nullable = false)
    private String telephone;
    @Column(unique = true,nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    @Column(name = "date_creation")
    private LocalDate dateCreation=LocalDate.now();
    @OneToMany(mappedBy = "user")
    private List<Notification>notifications;

}
