package spring.miniprojet.service;

import spring.miniprojet.entity.Cours;

import java.util.List;
import java.util.Optional;

public interface CoursService {

    List<Cours> findAll();

    Optional<Cours> findById(Long id);

    Optional<Cours> findByCode(String code);

    List<Cours> findByFormateurId(Long formateurId);

    List<Cours> findBySpecialiteId(Long specialiteId);

    List<Cours> findBySessionId(Long sessionId);

    List<Cours> findByGroupeId(Long groupeId);

    List<Cours> findByEtudiantId(Long etudiantId);

    List<Cours> search(String keyword);

    Cours save(Cours cours);

    Cours update(Long id, Cours cours);

    void delete(Long id);

    void assignerFormateur(Long coursId, Long formateurId);

    void assignerGroupe(Long coursId, Long groupeId);

    void retirerGroupe(Long coursId, Long groupeId);

    boolean existsByCode(String code);

    Long countEtudiantsInscrits(Long coursId);

    Double getTauxReussite(Long coursId);

    long count();

    List<java.util.Map<String, Object>> findCoursLesPlusSuivis(int limit);
}
