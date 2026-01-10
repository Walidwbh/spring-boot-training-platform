package spring.miniprojet.service;

import spring.miniprojet.entity.Formateur;

import java.util.List;
import java.util.Optional;

public interface FormateurService {

    List<Formateur> findAll();

    Optional<Formateur> findById(Long id);

    Optional<Formateur> findByEmail(String email);

    Optional<Formateur> findByUserId(Long userId);

    List<Formateur> findBySpecialite(String specialite);

    List<Formateur> search(String keyword);

    Formateur save(Formateur formateur);

    Formateur update(Long id, Formateur formateur);

    void delete(Long id);

    boolean existsByEmail(String email);

    long count();
}
