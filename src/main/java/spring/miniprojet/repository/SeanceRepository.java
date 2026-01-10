package spring.miniprojet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring.miniprojet.entity.Cours;
import spring.miniprojet.entity.Seance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

        List<Seance> findByCours(Cours cours);

        List<Seance> findByDateSeance(LocalDate dateSeance);

        List<Seance> findBySalle(String salle);

        @Query("SELECT s FROM Seance s WHERE s.dateSeance BETWEEN :dateDebut AND :dateFin ORDER BY s.dateSeance, s.heureDebut")
        List<Seance> findByDateSeanceBetween(@Param("dateDebut") LocalDate dateDebut,
                        @Param("dateFin") LocalDate dateFin);

        @Query("SELECT s FROM Seance s WHERE s.cours.formateur.id = :formateurId AND s.dateSeance = :date")
        List<Seance> findByFormateurIdAndDate(@Param("formateurId") Long formateurId, @Param("date") LocalDate date);

        @Query("SELECT s FROM Seance s JOIN s.cours.groupes g JOIN g.etudiants e WHERE e.id = :etudiantId AND s.dateSeance BETWEEN :dateDebut AND :dateFin ORDER BY s.dateSeance, s.heureDebut")
        List<Seance> findByEtudiantIdAndDateBetween(@Param("etudiantId") Long etudiantId,
                        @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);

        // Vérification des conflits d'horaires
        @Query("SELECT s FROM Seance s WHERE s.cours.formateur.id = :formateurId AND s.dateSeance = :date " +
                        "AND ((s.heureDebut < :heureFin AND s.heureFin > :heureDebut))")
        List<Seance> findConflitsFormateur(@Param("formateurId") Long formateurId, @Param("date") LocalDate date,
                        @Param("heureDebut") LocalTime heureDebut, @Param("heureFin") LocalTime heureFin);

        @Query("SELECT s FROM Seance s WHERE s.salle = :salle AND s.dateSeance = :date " +
                        "AND ((s.heureDebut < :heureFin AND s.heureFin > :heureDebut))")
        List<Seance> findConflitsSalle(@Param("salle") String salle, @Param("date") LocalDate date,
                        @Param("heureDebut") LocalTime heureDebut, @Param("heureFin") LocalTime heureFin);

        @Query("SELECT DISTINCT s FROM Seance s JOIN s.cours.groupes g WHERE g.id IN :groupeIds AND s.dateSeance = :date "
                        +
                        "AND ((s.heureDebut < :heureFin AND s.heureFin > :heureDebut))")
        List<Seance> findConflitsGroupes(@Param("groupeIds") List<Long> groupeIds, @Param("date") LocalDate date,
                        @Param("heureDebut") LocalTime heureDebut, @Param("heureFin") LocalTime heureFin);

        @Query("SELECT s FROM Seance s WHERE s.cours.formateur.id = :formateurId AND s.dateSeance BETWEEN :dateDebut AND :dateFin ORDER BY s.dateSeance, s.heureDebut")
        List<Seance> findByFormateurIdAndDateRange(@Param("formateurId") Long formateurId,
                        @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin);
}
