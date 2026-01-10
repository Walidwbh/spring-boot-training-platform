package spring.miniprojet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.miniprojet.entity.Cours;
import spring.miniprojet.entity.Groupe;
import spring.miniprojet.entity.Seance;
import spring.miniprojet.repository.CoursRepository;
import spring.miniprojet.repository.SeanceRepository;
import spring.miniprojet.service.SeanceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeanceServiceImpl implements SeanceService {

    private final SeanceRepository seanceRepository;
    private final CoursRepository coursRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findAll() {
        return seanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seance> findById(Long id) {
        return seanceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findByCoursId(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        return seanceRepository.findByCours(cours);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findByDateSeance(LocalDate date) {
        return seanceRepository.findByDateSeance(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findByDateRange(LocalDate dateDebut, LocalDate dateFin) {
        return seanceRepository.findByDateSeanceBetween(dateDebut, dateFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findEmploiDuTempsEtudiant(Long etudiantId, LocalDate dateDebut, LocalDate dateFin) {
        return seanceRepository.findByEtudiantIdAndDateBetween(etudiantId, dateDebut, dateFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findEmploiDuTempsFormateur(Long formateurId, LocalDate date) {
        return seanceRepository.findByFormateurIdAndDate(formateurId, date);
    }

    @Override
    public Seance save(Seance seance) {
        return seanceRepository.save(seance);
    }

    @Override
    public Seance planifier(Long coursId, LocalDate date, LocalTime heureDebut, LocalTime heureFin, String salle,
            Seance.TypeSeance type) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Vérifier les conflits de formateur
        if (cours.getFormateur() != null
                && hasConflitFormateur(cours.getFormateur().getId(), date, heureDebut, heureFin)) {
            throw new RuntimeException("Le formateur a déjà une séance à cette heure");
        }

        // Vérifier les conflits de salle
        if (hasConflitSalle(salle, date, heureDebut, heureFin)) {
            throw new RuntimeException("La salle est déjà occupée à cette heure");
        }

        // Vérifier les conflits de groupes (étudiants)
        if (!cours.getGroupes().isEmpty()) {
            List<Long> groupeIds = cours.getGroupes().stream()
                    .map(Groupe::getId)
                    .collect(Collectors.toList());

            List<Seance> conflitsGroupes = seanceRepository.findConflitsGroupes(groupeIds, date, heureDebut, heureFin);
            if (!conflitsGroupes.isEmpty()) {
                throw new RuntimeException(
                        "Un ou plusieurs groupes d'étudiants (et donc les étudiants associés) ont déjà cours à cette heure");
            }
        }

        Seance seance = Seance.builder()
                .cours(cours)
                .dateSeance(date)
                .heureDebut(heureDebut)
                .heureFin(heureFin)
                .salle(salle)
                .type(type)
                .build();

        return seanceRepository.save(seance);
    }

    @Override
    public Seance update(Long id, Seance seance) {
        return seanceRepository.findById(id)
                .map(existing -> {
                    existing.setDateSeance(seance.getDateSeance());
                    existing.setHeureDebut(seance.getHeureDebut());
                    existing.setHeureFin(seance.getHeureFin());
                    existing.setSalle(seance.getSalle());
                    existing.setType(seance.getType());
                    return seanceRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
    }

    @Override
    public void delete(Long id) {
        seanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflitFormateur(Long formateurId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        List<Seance> conflits = seanceRepository.findConflitsFormateur(formateurId, date, heureDebut, heureFin);
        return !conflits.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflitSalle(String salle, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        List<Seance> conflits = seanceRepository.findConflitsSalle(salle, date, heureDebut, heureFin);
        return !conflits.isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seance> findByFormateurIdAndDateRange(Long formateurId, LocalDate dateDebut, LocalDate dateFin) {
        return seanceRepository.findByFormateurIdAndDateRange(formateurId, dateDebut, dateFin);
    }
}
