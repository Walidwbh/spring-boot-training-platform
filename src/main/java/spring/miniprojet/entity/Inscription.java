package spring.miniprojet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "etudiant_id", "cours_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutInscription statut = StatutInscription.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cours cours;

    public enum StatutInscription {
        EN_ATTENTE,
        CONFIRMEE,
        ANNULEE
    }
}
