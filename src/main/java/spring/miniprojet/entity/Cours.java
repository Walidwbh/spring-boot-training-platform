package spring.miniprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer credits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formateur_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Formateur formateur;

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

    @ManyToMany
    @JoinTable(name = "cours_groupes", joinColumns = @JoinColumn(name = "cours_id"), inverseJoinColumns = @JoinColumn(name = "groupe_id"))
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Groupe> groupes = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Inscription> inscriptions = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Note> notes = new HashSet<>();

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Seance> seances = new HashSet<>();
}
