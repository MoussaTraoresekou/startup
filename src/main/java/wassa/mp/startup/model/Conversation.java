package wassa.mp.startup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations",uniqueConstraints = {@UniqueConstraint(
                        name = "uk_conversation_investisseur_projet", columnNames = {"investisseur_id", "projet_id"})},
        indexes = {
                @Index(
                        name = "idx_conversation_porteur",
                        columnList = "porteur_id"
                )
        }
)
@Data  @AllArgsConstructor @NoArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investisseur_id", nullable = false)
    private Investisseur investisseur;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "porteur_id", nullable = false)
    private Porteur porteur;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}

