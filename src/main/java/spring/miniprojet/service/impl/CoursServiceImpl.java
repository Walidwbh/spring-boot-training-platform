package spring.miniprojet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.miniprojet.entity.Cours;
import spring.miniprojet.entity.Formateur;
import spring.miniprojet.entity.Groupe;
import spring.miniprojet.repository.*;
import spring.miniprojet.service.CoursService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CoursServiceImpl implements CoursService {

    private final CoursRepository coursRepository;
    private final FormateurRepository formateurRepository;
    private final GroupeRepository groupeRepository;
    private final NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cours> findAll() {
        return coursRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cours> findById(Long id) {
        return coursRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cours> findByCode(String code) {
        return coursRepository.findByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cours> findByFormateurId(Long formateurId) {
        Formateur formateur = formateurRepository.findById(formateurId)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
        return coursRepository.findByFormateur(formateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cours> findBySpecialiteId(Long specialiteId) {
        return coursRepository.findAll().stream()
                .filter(c -> c.getSpecialite() != null && c.getSpecialite().getId().equals(specialiteId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cours> findBySessionId(Long sessionId) {
        return coursRepository.findAll().stream()
                .filter(c -> c.getSession() != null && c.getSession().getId().equals(sessionId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cours> findByGroupeId(Long groupeId) {
        return coursRepository.findByGroupeId(groupeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cours> findByEtudiantId(Long etudiantId) {
        return coursRepository.findByEtudiantId(etudiantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cours> search(String keyword) {
        return coursRepository.findByTitreContainingIgnoreCase(keyword);
    }

    @Override
    public Cours save(Cours cours) {
        return coursRepository.save(cours);
    }

    @Override
    public Cours update(Long id, Cours cours) {
        return coursRepository.findById(id)
                .map(existing -> {
                    existing.setTitre(cours.getTitre());
                    existing.setDescription(cours.getDescription());
                    existing.setCredits(cours.getCredits());
                    existing.setFormateur(cours.getFormateur());
                    existing.setSpecialite(cours.getSpecialite());
                    existing.setSession(cours.getSession());
                    return coursRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Cours non trouvé avec l'id: " + id));
    }

    @Override
    public void delete(Long id) {
        coursRepository.deleteById(id);
    }

    @Override
    public void assignerFormateur(Long coursId, Long formateurId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        Formateur formateur = formateurRepository.findById(formateurId)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
        cours.setFormateur(formateur);
        coursRepository.save(cours);
    }

    @Override
    public void assignerGroupe(Long coursId, Long groupeId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        cours.getGroupes().add(groupe);
        coursRepository.save(cours);
    }

    @Override
    public void retirerGroupe(Long coursId, Long groupeId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        cours.getGroupes().removeIf(g -> g.getId().equals(groupeId));
        coursRepository.save(cours);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return coursRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countEtudiantsInscrits(Long coursId) {
        return coursRepository.countEtudiantsInscrits(coursId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTauxReussite(Long coursId) {
        Long totalNotes = noteRepository.countNotesParCours(coursId);
        if (totalNotes == 0)
            return 0.0;
        Long reussites = noteRepository.countReussiteByCoursId(coursId, 10.0);
        return (reussites.doubleValue() / totalNotes.doubleValue()) * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return coursRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> findCoursLesPlusSuivis(int limit) {
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.util.List<Cours> allCours = coursRepository.findAll();

        allCours.sort((c1, c2) -> {
            Long count1 = coursRepository.countEtudiantsInscrits(c1.getId());
            Long count2 = coursRepository.countEtudiantsInscrits(c2.getId());
            return count2.compareTo(count1);
        });

        for (int i = 0; i < Math.min(limit, allCours.size()); i++) {
            Cours c = allCours.get(i);
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("cours", c);
            map.put("nombreInscrits", coursRepository.countEtudiantsInscrits(c.getId()));
            result.add(map);
        }
        return result;
    }
}
