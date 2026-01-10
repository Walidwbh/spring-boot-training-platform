package spring.miniprojet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.miniprojet.entity.Etudiant;
import spring.miniprojet.repository.EtudiantRepository;
import spring.miniprojet.repository.NoteRepository;
import spring.miniprojet.service.EtudiantService;
import spring.miniprojet.service.UserService;
import spring.miniprojet.entity.User;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final NoteRepository noteRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<Etudiant> findAll() {
        return etudiantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Etudiant> findById(Long id) {
        return etudiantRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Etudiant> findByMatricule(String matricule) {
        return etudiantRepository.findByMatricule(matricule);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Etudiant> findByEmail(String email) {
        return etudiantRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Etudiant> findByUserId(Long userId) {
        return etudiantRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Etudiant> findByCoursId(Long coursId) {
        return etudiantRepository.findByCoursId(coursId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Etudiant> search(String keyword) {
        return etudiantRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public Etudiant save(Etudiant etudiant) {
        if (etudiant.getMatricule() == null || etudiant.getMatricule().isEmpty()) {
            etudiant.setMatricule(generateMatricule());
        }
        if (etudiant.getDateInscription() == null) {
            etudiant.setDateInscription(LocalDate.now());
        }

        // Create User account if not exists
        if (etudiant.getUser() == null) {
            // Ensure email is present
            if (etudiant.getEmail() == null || etudiant.getEmail().isEmpty()) {
                throw new RuntimeException("L'email est obligatoire pour créer un compte utilisateur");
            }

            // Check if user with this email already exists
            if (userService.existsByEmail(etudiant.getEmail())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }

            User user = User.builder()
                    .username(etudiant.getEmail()) // Use email as username
                    .email(etudiant.getEmail())
                    .password(etudiant.getMatricule()) // Use matricule as default password
                    .role(User.Role.ETUDIANT)
                    .enabled(true)
                    .build();

            user = userService.save(user); // Encodes password
            etudiant.setUser(user);
        }

        return etudiantRepository.save(etudiant);
    }

    @Override
    public Etudiant update(Long id, Etudiant etudiant) {
        return etudiantRepository.findById(id)
                .map(existing -> {
                    existing.setNom(etudiant.getNom());
                    existing.setPrenom(etudiant.getPrenom());
                    existing.setEmail(etudiant.getEmail());
                    existing.setGroupe(etudiant.getGroupe());
                    return etudiantRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'id: " + id));
    }

    @Override
    public void delete(Long id) {
        etudiantRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMatricule(String matricule) {
        return etudiantRepository.existsByMatricule(matricule);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return etudiantRepository.existsByEmail(email);
    }

    @Override
    public String generateMatricule() {
        String year = String.valueOf(Year.now().getValue());
        long count = etudiantRepository.count() + 1;
        return "ETU" + year + String.format("%04d", count);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateMoyenneGenerale(Long etudiantId) {
        return noteRepository.calculateMoyenneByEtudiantId(etudiantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return etudiantRepository.count();
    }
}
