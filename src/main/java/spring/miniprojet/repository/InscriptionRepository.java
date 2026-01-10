package spring.miniprojet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring.miniprojet.entity.Cours;
import spring.miniprojet.entity.Etudiant;
import spring.miniprojet.entity.Inscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByEtudiant(Etudiant etudiant);

    List<Inscription> findByCours(Cours cours);

    Optional<Inscription> findByEtudiantAndCours(Etudiant etudiant, Cours cours);

    List<Inscription> findByStatut(Inscription.StatutInscription statut);

    @Query("SELECT i FROM Inscription i WHERE i.etudiant.id = :etudiantId AND i.statut = 'CONFIRMEE'")
    List<Inscription> findActiveByEtudiantId(@Param("etudiantId") Long etudiantId);

    @Query("SELECT i FROM Inscription i WHERE i.cours.id = :coursId AND i.statut = 'CONFIRMEE'")
    List<Inscription> findActiveByCoursId(@Param("coursId") Long coursId);

    boolean existsByEtudiantAndCours(Etudiant etudiant, Cours cours);

    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.cours.id = :coursId AND i.statut = 'CONFIRMEE'")
    Long countByCoursIdAndStatutConfirmee(@Param("coursId") Long coursId);

    @Query("SELECT i FROM Inscription i WHERE i.cours.id = :coursId AND i.statut = :statut")
    List<Inscription> findByCoursIdAndStatut(@Param("coursId") Long coursId,
            @Param("statut") Inscription.StatutInscription statut);
}
