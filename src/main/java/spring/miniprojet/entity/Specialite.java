package spring.miniprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "specialites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(unique = true)
    private String nom;

    private String description;

    @OneToMany(mappedBy = "specialite")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Cours> cours = new HashSet<>();

    @OneToMany(mappedBy = "specialite")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Groupe> groupes = new HashSet<>();
}
