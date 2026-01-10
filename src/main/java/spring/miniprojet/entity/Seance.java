package spring.miniprojet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "seances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_seance")
    private LocalDate dateSeance;

    @Column(name = "heure_debut")
    private LocalTime heureDebut;

    @Column(name = "heure_fin")
    private LocalTime heureFin;

    private String salle;

    @Enumerated(EnumType.STRING)
    private TypeSeance type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cours cours;

    public enum TypeSeance {
        COURS_MAGISTRAL,
        TD,
        TP,
        EXAMEN
    }
}
