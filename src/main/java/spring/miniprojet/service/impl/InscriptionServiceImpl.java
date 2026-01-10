package spring.miniprojet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.miniprojet.entity.Cours;
import spring.miniprojet.entity.Etudiant;
import spring.miniprojet.entity.Inscription;
import spring.miniprojet.repository.CoursRepository;
import spring.miniprojet.repository.EtudiantRepository;
import spring.miniprojet.repository.InscriptionRepository;
import spring.miniprojet.service.EmailService;
import spring.miniprojet.service.InscriptionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InscriptionServiceImpl implements InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findAll() {
        return inscriptionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inscription> findById(Long id) {
        return inscriptionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findByEtudiantId(Long etudiantId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        return inscriptionRepository.findByEtudiant(etudiant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findByCoursId(Long coursId) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        return inscriptionRepository.findByCours(cours);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inscription> findByEtudiantAndCours(Long etudiantId, Long coursId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        return inscriptionRepository.findByEtudiantAndCours(etudiant, cours);
    }

    @Override
    public Inscription inscrire(Long etudiantId, Long coursId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        if (inscriptionRepository.existsByEtudiantAndCours(etudiant, cours)) {
            throw new RuntimeException("L'étudiant est déjà inscrit à ce cours");
        }

        Inscription inscription = Inscription.builder()
                .etudiant(etudiant)
                .cours(cours)
                .dateInscription(LocalDate.now())
                .statut(Inscription.StatutInscription.EN_ATTENTE)
                .build();

        Inscription saved = inscriptionRepository.save(inscription);

        // Envoyer notification à l'étudiant
        if (etudiant.getEmail() != null) {
            emailService.sendInscriptionConfirmation(etudiant.getEmail(), etudiant.getNomComplet(), cours.getTitre());
        }

        // Notifier le formateur
        if (cours.getFormateur() != null && cours.getFormateur().getEmail() != null) {
            emailService.notifyFormateurInscription(
                    cours.getFormateur().getEmail(),
                    cours.getFormateur().getNomComplet(),
                    etudiant.getNomComplet(),
                    cours.getTitre(),
                    true);
        }

        return saved;
    }

    @Override
    public Inscription confirmer(Long inscriptionId) {
        return inscriptionRepository.findById(inscriptionId)
                .map(inscription -> {
                    inscription.setStatut(Inscription.StatutInscription.CONFIRMEE);
                    return inscriptionRepository.save(inscription);
                })
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
    }

    @Override
    public Inscription annuler(Long inscriptionId) {
        return inscriptionRepository.findById(inscriptionId)
                .map(inscription -> {
                    inscription.setStatut(Inscription.StatutInscription.ANNULEE);

                    // Notifier le formateur de la désinscription
                    Cours cours = inscription.getCours();
                    Etudiant etudiant = inscription.getEtudiant();
                    if (cours.getFormateur() != null && cours.getFormateur().getEmail() != null) {
                        emailService.notifyFormateurInscription(
                                cours.getFormateur().getEmail(),
                                cours.getFormateur().getNomComplet(),
                                etudiant.getNomComplet(),
                                cours.getTitre(),
                                false);
                    }

                    return inscriptionRepository.save(inscription);
                })
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
    }

    @Override
    public void delete(Long id) {
        inscriptionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEtudiantInscrit(Long etudiantId, Long coursId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
        Cours cours = coursRepository.findById(coursId).orElse(null);
        if (etudiant == null || cours == null)
            return false;
        return inscriptionRepository.existsByEtudiantAndCours(etudiant, cours);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return inscriptionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Inscription> findByStatut(Inscription.StatutInscription statut) {
        return inscriptionRepository.findByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Inscription> findByCoursIdAndStatut(Long coursId, Inscription.StatutInscription statut) {
        return inscriptionRepository.findByCoursIdAndStatut(coursId, statut);
    }
}
