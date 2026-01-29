package spring.miniprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Classe abstraite représentant une personne (Etudiant ou Formateur).
 * Utilise @MappedSuperclass pour partager les champs communs sans créer de
 * table séparée.
 * 
 * Type d'héritage: MappedSuperclass
 * - Pas de table "personne" dans la base de données
 * - Les champs sont hérités dans les tables "etudiants" et "formateurs"
 * - Pas de requêtes polymorphiques possibles
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Personne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    private String telephone;

    private String adresse;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

    /**
     * Retourne le nom complet de la personne (prénom + nom)
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
