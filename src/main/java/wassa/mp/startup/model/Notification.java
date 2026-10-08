package wassa.mp.startup.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    private boolean lu;
    private LocalDateTime dateCreation;
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User user;
}
