package spring.miniprojet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité Formateur qui hérite de Personne.
 * 
 * Type d'héritage JPA: @MappedSuperclass
 * - La table "formateurs" contient tous les champs hérités de Personne
 * - Plus les champs spécifiques à Formateur (specialite, cours)
 */
@Entity
@Table(name = "formateurs")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Formateur extends Personne {

    private String specialite;

    @OneToMany(mappedBy = "formateur")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<Cours> cours = new HashSet<>();
}
