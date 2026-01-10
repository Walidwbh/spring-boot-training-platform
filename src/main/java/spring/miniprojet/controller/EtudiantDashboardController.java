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
import java.util.List;

@Controller
@RequestMapping("/etudiant")
@RequiredArgsConstructor
public class EtudiantDashboardController {

    private final EtudiantService etudiantService;
    private final CoursService coursService;
    private final InscriptionService inscriptionService;
    private final NoteService noteService;
    private final SeanceService seanceService;
    private final UserService userService;

    private Etudiant getCurrentEtudiant(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user != null) {
            return etudiantService.findByUserId(user.getId()).orElse(null);
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null) {
            return "redirect:/login";
        }

        List<Cours> mesCours = coursService.findByEtudiantId(etudiant.getId());
        List<Note> mesNotes = noteService.findByEtudiantId(etudiant.getId());
        Double moyenne = etudiantService.calculateMoyenneGenerale(etudiant.getId());

        // Emploi du temps de la semaine
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        List<Seance> seancesSemaine = seanceService.findEmploiDuTempsEtudiant(etudiant.getId(), startOfWeek, endOfWeek);

        model.addAttribute("etudiant", etudiant);
        model.addAttribute("mesCours", mesCours);
        model.addAttribute("mesNotes", mesNotes);
        model.addAttribute("moyenne", moyenne);
        model.addAttribute("seancesSemaine", seancesSemaine);
        model.addAttribute("nombreCours", mesCours.size());

        return "etudiant/dashboard";
    }

    @GetMapping("/cours")
    public String mesCours(Model model, Authentication auth) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        List<Cours> mesCours = coursService.findByEtudiantId(etudiant.getId());
        model.addAttribute("etudiant", etudiant);
        model.addAttribute("mesCours", mesCours);
        return "etudiant/cours";
    }

    @GetMapping("/cours-disponibles")
    public String coursDisponibles(Model model, Authentication auth) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        List<Cours> tousLesCours = coursService.findAll();
        List<Cours> coursInscrits = coursService.findByEtudiantId(etudiant.getId());

        // Filtrer les cours non encore inscrits
        tousLesCours.removeAll(coursInscrits);

        model.addAttribute("etudiant", etudiant);
        model.addAttribute("coursDisponibles", tousLesCours);
        return "etudiant/cours-disponibles";
    }

    @PostMapping("/inscription/{coursId}")
    public String inscrireAuCours(@PathVariable Long coursId, Authentication auth,
            RedirectAttributes redirectAttributes) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        try {
            inscriptionService.inscrire(etudiant.getId(), coursId);
            redirectAttributes.addFlashAttribute("success", "Demande d'inscription envoyée avec succès!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/etudiant/cours-disponibles";
    }

    @GetMapping("/notes")
    public String mesNotes(Model model, Authentication auth) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        List<Note> mesNotes = noteService.findByEtudiantId(etudiant.getId());
        Double moyenne = etudiantService.calculateMoyenneGenerale(etudiant.getId());

        long notesSup10 = mesNotes.stream().filter(n -> n.getValeur() != null && n.getValeur() >= 10).count();
        long notesInf10 = mesNotes.stream().filter(n -> n.getValeur() != null && n.getValeur() < 10).count();

        model.addAttribute("etudiant", etudiant);
        model.addAttribute("mesNotes", mesNotes);
        model.addAttribute("moyenne", moyenne);
        model.addAttribute("notesSup10", notesSup10);
        model.addAttribute("notesInf10", notesInf10);
        return "etudiant/notes";
    }

    @GetMapping("/emploi-du-temps")
    public String emploiDuTemps(Model model, Authentication auth,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        LocalDate start = dateDebut != null ? LocalDate.parse(dateDebut)
                : LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate end = dateFin != null ? LocalDate.parse(dateFin) : start.plusDays(6);

        List<Seance> seances = seanceService.findEmploiDuTempsEtudiant(etudiant.getId(), start, end);

        model.addAttribute("etudiant", etudiant);
        model.addAttribute("seances", seances);
        model.addAttribute("dateDebut", start);
        model.addAttribute("dateFin", end);
        return "etudiant/emploi-du-temps";
    }

    @GetMapping("/profil")
    public String profil(Model model, Authentication auth) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        model.addAttribute("etudiant", etudiant);
        return "etudiant/profil";
    }

    @PostMapping("/profil/update")
    public String updateProfil(@RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String adresse,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        try {
            // Check if email changed and is available
            if (!etudiant.getEmail().equals(email) && userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé.");
                return "redirect:/etudiant/profil";
            }

            etudiant.setNom(nom);
            etudiant.setPrenom(prenom);
            etudiant.setEmail(email);
            etudiant.setTelephone(telephone);
            etudiant.setAdresse(adresse);
            etudiantService.save(etudiant);

            // Sync with User
            User user = etudiant.getUser();
            if (!user.getEmail().equals(email)) {
                user.setEmail(email);
                userService.update(user.getId(), user);
            }

            redirectAttributes.addFlashAttribute("success", "Profil mis à jour!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/etudiant/profil";
    }

    @PostMapping("/profil/password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        Etudiant etudiant = getCurrentEtudiant(auth);
        if (etudiant == null)
            return "redirect:/login";

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas!");
            return "redirect:/etudiant/profil";
        }

        try {
            userService.changePassword(etudiant.getUser().getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/etudiant/profil";
    }
}
