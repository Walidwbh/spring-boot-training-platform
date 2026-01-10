package spring.miniprojet.service;

import spring.miniprojet.entity.Etudiant;

import java.util.List;
import java.util.Optional;

public interface EtudiantService {

    List<Etudiant> findAll();

    Optional<Etudiant> findById(Long id);

    Optional<Etudiant> findByMatricule(String matricule);

    Optional<Etudiant> findByEmail(String email);

    Optional<Etudiant> findByUserId(Long userId);

    List<Etudiant> findByCoursId(Long coursId);

    List<Etudiant> search(String keyword);

    Etudiant save(Etudiant etudiant);

    Etudiant update(Long id, Etudiant etudiant);

    void delete(Long id);

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);

    String generateMatricule();

    Double calculateMoyenneGenerale(Long etudiantId);

    long count();
}
