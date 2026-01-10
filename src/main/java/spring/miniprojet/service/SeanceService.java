package spring.miniprojet.service;

import spring.miniprojet.entity.Seance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SeanceService {

    List<Seance> findAll();

    Optional<Seance> findById(Long id);

    List<Seance> findByCoursId(Long coursId);

    List<Seance> findByDateSeance(LocalDate date);

    List<Seance> findByDateRange(LocalDate dateDebut, LocalDate dateFin);

    List<Seance> findEmploiDuTempsEtudiant(Long etudiantId, LocalDate dateDebut, LocalDate dateFin);

    List<Seance> findEmploiDuTempsFormateur(Long formateurId, LocalDate date);

    Seance save(Seance seance);

    Seance planifier(Long coursId, LocalDate date, LocalTime heureDebut, LocalTime heureFin, String salle,
            Seance.TypeSeance type);

    Seance update(Long id, Seance seance);

    void delete(Long id);

    boolean hasConflitFormateur(Long formateurId, LocalDate date, LocalTime heureDebut, LocalTime heureFin);

    boolean hasConflitSalle(String salle, LocalDate date, LocalTime heureDebut, LocalTime heureFin);

    List<Seance> findByFormateurIdAndDateRange(Long formateurId, LocalDate dateDebut, LocalDate dateFin);
}
