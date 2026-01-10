package spring.miniprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "notes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "etudiant_id", "cours_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin(value = "0.0", message = "La note doit être supérieure ou égale à 0")
    @DecimalMax(value = "20.0", message = "La note doit être inférieure ou égale à 20")
    private Double valeur;

    private String commentaire;

    @Column(name = "date_saisie")
    private LocalDate dateSaisie;

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
}
