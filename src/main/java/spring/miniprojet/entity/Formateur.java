package spring.miniprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "formateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    private String specialite;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    @Column(unique = true)
    private String email;

    private String telephone;
    private String adresse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "formateur", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Cours> cours = new HashSet<>();

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
