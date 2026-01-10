package spring.miniprojet.service;

import spring.miniprojet.entity.Inscription;

import java.util.List;
import java.util.Optional;

public interface InscriptionService {

    List<Inscription> findAll();

    Optional<Inscription> findById(Long id);

    List<Inscription> findByEtudiantId(Long etudiantId);

    List<Inscription> findByCoursId(Long coursId);

    Optional<Inscription> findByEtudiantAndCours(Long etudiantId, Long coursId);

    Inscription inscrire(Long etudiantId, Long coursId);

    Inscription confirmer(Long inscriptionId);

    Inscription annuler(Long inscriptionId);

    void delete(Long id);

    boolean isEtudiantInscrit(Long etudiantId, Long coursId);

    long count();

    List<Inscription> findByStatut(Inscription.StatutInscription statut);

    List<Inscription> findByCoursIdAndStatut(Long coursId, Inscription.StatutInscription statut);
}
