package spring.miniprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groupes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialite_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Specialite specialite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SessionPedagogique session;

    @OneToMany(mappedBy = "groupe")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Etudiant> etudiants = new HashSet<>();

    @ManyToMany(mappedBy = "groupes")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Cours> cours = new HashSet<>();
}
