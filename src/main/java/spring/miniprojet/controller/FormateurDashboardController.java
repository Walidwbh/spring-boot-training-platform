package spring.miniprojet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.miniprojet.entity.*;
import spring.miniprojet.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/formateur")
@RequiredArgsConstructor
public class FormateurDashboardController {

    private final FormateurService formateurService;
    private final CoursService coursService;
    private final NoteService noteService;
    private final SeanceService seanceService;
    private final UserService userService;
    private final EtudiantService etudiantService;
    private final InscriptionService inscriptionService;

    private Formateur getCurrentFormateur(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user != null) {
            return formateurService.findByUserId(user.getId()).orElse(null);
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null) {
            return "redirect:/login";
        }

        List<Cours> mesCours = coursService.findByFormateurId(formateur.getId());

        // Statistiques
        int totalEtudiants = 0;
        for (Cours cours : mesCours) {
            totalEtudiants += coursService.countEtudiantsInscrits(cours.getId());
        }

        // Emploi du temps du jour
        LocalDate today = LocalDate.now();
        List<Seance> seancesAujourdhui = seanceService.findEmploiDuTempsFormateur(formateur.getId(), today);

        model.addAttribute("formateur", formateur);
        model.addAttribute("mesCours", mesCours);
        model.addAttribute("totalEtudiants", totalEtudiants);
        model.addAttribute("seancesAujourdhui", seancesAujourdhui);

        return "formateur/dashboard";
    }

    @GetMapping("/cours")
    public String mesCours(Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        List<Cours> mesCours = coursService.findByFormateurId(formateur.getId());

        // Stats par cours
        Map<Long, Map<String, Object>> statsParCours = new HashMap<>();
        for (Cours cours : mesCours) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("inscrits", coursService.countEtudiantsInscrits(cours.getId()));
            stats.put("tauxReussite", coursService.getTauxReussite(cours.getId()));
            statsParCours.put(cours.getId(), stats);
        }

        model.addAttribute("formateur", formateur);
        model.addAttribute("mesCours", mesCours);
        model.addAttribute("statsParCours", statsParCours);
        return "formateur/cours";
    }

    @GetMapping("/cours/{id}/etudiants")
    public String etudiantsDuCours(@PathVariable Long id, Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        Cours cours = coursService.findById(id).orElse(null);
        if (cours == null || !cours.getFormateur().getId().equals(formateur.getId())) {
            return "redirect:/formateur/cours";
        }

        List<Inscription> inscriptions = inscriptionService.findByCoursIdAndStatut(id,
                Inscription.StatutInscription.CONFIRMEE);
        List<Note> notes = noteService.findByCoursId(id);

        model.addAttribute("formateur", formateur);
        model.addAttribute("cours", cours);
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("notes", notes);
        return "formateur/cours-etudiants";
    }

    @GetMapping("/notes")
    public String saisieNotes(Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        List<Cours> mesCours = coursService.findByFormateurId(formateur.getId());

        model.addAttribute("formateur", formateur);
        model.addAttribute("mesCours", mesCours);
        return "formateur/notes";
    }

    @GetMapping("/notes/cours/{coursId}")
    public String notesParCours(@PathVariable Long coursId, Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        Cours cours = coursService.findById(coursId).orElse(null);
        if (cours == null || !cours.getFormateur().getId().equals(formateur.getId())) {
            return "redirect:/formateur/notes";
        }

        List<Inscription> inscriptions = inscriptionService.findByCoursIdAndStatut(coursId,
                Inscription.StatutInscription.CONFIRMEE);
        List<Note> notes = noteService.findByCoursId(coursId);

        // Map notes par étudiant
        Map<Long, Note> notesParEtudiant = new HashMap<>();
        for (Note note : notes) {
            notesParEtudiant.put(note.getEtudiant().getId(), note);
        }

        model.addAttribute("formateur", formateur);
        model.addAttribute("cours", cours);
        model.addAttribute("inscriptions", inscriptions);
        model.addAttribute("notesParEtudiant", notesParEtudiant);
        return "formateur/notes-cours";
    }

    @PostMapping("/notes/save")
    public String saveNote(@RequestParam Long etudiantId,
            @RequestParam Long coursId,
            @RequestParam Double valeur,
            @RequestParam(required = false) String commentaire,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        try {
            noteService.attribuerNote(etudiantId, coursId, valeur, commentaire);
            redirectAttributes.addFlashAttribute("success", "Note enregistrée avec succès!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/formateur/notes/cours/" + coursId;
    }

    @GetMapping("/emploi-du-temps")
    public String emploiDuTemps(Model model, Authentication auth,
            @RequestParam(required = false) String date) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        List<Seance> seances = seanceService.findEmploiDuTempsFormateur(formateur.getId(), selectedDate);

        // Séances de la semaine
        LocalDate startOfWeek = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        List<Seance> seancesSemaine = seanceService.findByFormateurIdAndDateRange(formateur.getId(), startOfWeek,
                endOfWeek);

        model.addAttribute("formateur", formateur);
        model.addAttribute("seances", seances);
        model.addAttribute("seancesSemaine", seancesSemaine);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("startOfWeek", startOfWeek);
        model.addAttribute("endOfWeek", endOfWeek);
        return "formateur/emploi-du-temps";
    }

    @GetMapping("/seances")
    public String gererSeances(Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        List<Cours> mesCours = coursService.findByFormateurId(formateur.getId());

        model.addAttribute("formateur", formateur);
        model.addAttribute("mesCours", mesCours);
        return "formateur/seances";
    }

    @PostMapping("/seances/planifier")
    public String planifierSeance(@RequestParam Long coursId,
            @RequestParam String date,
            @RequestParam String heureDebut,
            @RequestParam String heureFin,
            @RequestParam String salle,
            @RequestParam String type,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        try {
            seanceService.planifier(
                    coursId,
                    LocalDate.parse(date),
                    LocalTime.parse(heureDebut),
                    LocalTime.parse(heureFin),
                    salle,
                    Seance.TypeSeance.valueOf(type));
            redirectAttributes.addFlashAttribute("success", "Séance planifiée avec succès!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/formateur/seances";
    }

    @GetMapping("/profil")
    public String profil(Model model, Authentication auth) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        model.addAttribute("formateur", formateur);
        return "formateur/profil";
    }

    @PostMapping("/profil/update")
    public String updateProfil(@RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String specialite,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String adresse,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        try {
            // Check if email changed and is available
            if (!formateur.getEmail().equals(email) && userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé.");
                return "redirect:/formateur/profil";
            }

            formateur.setNom(nom);
            formateur.setPrenom(prenom);
            formateur.setEmail(email);
            formateur.setSpecialite(specialite);
            formateur.setTelephone(telephone);
            formateur.setAdresse(adresse);
            formateurService.save(formateur);

            // Sync with User
            User user = formateur.getUser();
            if (!user.getEmail().equals(email)) {
                user.setEmail(email);
                userService.update(user.getId(), user);
            }

            redirectAttributes.addFlashAttribute("success", "Profil mis à jour!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/formateur/profil";
    }

    @PostMapping("/profil/password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Formateur formateur = getCurrentFormateur(auth);
        if (formateur == null)
            return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas!");
            return "redirect:/formateur/profil";
        }

        try {
            userService.changePassword(formateur.getUser().getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/formateur/profil";
    }
}
