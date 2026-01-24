package spring.miniprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sessions_pedagogiques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionPedagogique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    private TypeSession type;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Builder.Default
    private Boolean active = false;

    @OneToMany(mappedBy = "session")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<Cours> cours = new HashSet<>();

    @OneToMany(mappedBy = "session")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<Groupe> groupes = new HashSet<>();

    public enum TypeSession {
        SEMESTRE_1,
        SEMESTRE_2,
        ANNEE_SCOLAIRE,
        SESSION_ETE
    }
}
