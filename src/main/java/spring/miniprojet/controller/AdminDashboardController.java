package spring.miniprojet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.miniprojet.entity.*;
import spring.miniprojet.service.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final EtudiantService etudiantService;
    private final FormateurService formateurService;
    private final CoursService coursService;
    private final InscriptionService inscriptionService;
    private final NoteService noteService;
    private final SpecialiteService specialiteService;
    private final GroupeService groupeService;
    private final SessionPedagogiqueService sessionService;
    private final SeanceService seanceService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Statistiques globales
        long totalEtudiants = etudiantService.count();
        long totalFormateurs = formateurService.count();
        long totalCours = coursService.count();
        long totalInscriptions = inscriptionService.count();

        // Cours les plus suivis
        List<Map<String, Object>> coursPopulaires = coursService.findCoursLesPlusSuivis(5);

        // Inscriptions en attente
        List<Inscription> inscriptionsEnAttente = inscriptionService
                .findByStatut(Inscription.StatutInscription.EN_ATTENTE);

        model.addAttribute("totalEtudiants", totalEtudiants);
        model.addAttribute("totalFormateurs", totalFormateurs);
        model.addAttribute("totalCours", totalCours);
        model.addAttribute("totalInscriptions", totalInscriptions);
        model.addAttribute("coursPopulaires", coursPopulaires);
        model.addAttribute("inscriptionsEnAttente", inscriptionsEnAttente);

        return "admin/dashboard";
    }

    // ===================== SPECIALITES =====================
    @GetMapping("/specialites")
    public String specialites(Model model) {
        model.addAttribute("specialites", specialiteService.findAll());
        return "admin/specialites/list";
    }

    @GetMapping("/specialites/new")
    public String newSpecialite(Model model) {
        model.addAttribute("specialite", new Specialite());
        return "admin/specialites/form";
    }

    @GetMapping("/specialites/edit/{id}")
    public String editSpecialite(@PathVariable Long id, Model model) {
        Specialite specialite = specialiteService.findById(id).orElse(null);
        if (specialite == null)
            return "redirect:/admin/specialites";
        model.addAttribute("specialite", specialite);
        return "admin/specialites/form";
    }

    @PostMapping("/specialites/save")
    public String saveSpecialite(@ModelAttribute Specialite specialite, RedirectAttributes redirectAttributes) {
        specialiteService.save(specialite);
        redirectAttributes.addFlashAttribute("success", "Spécialité enregistrée!");
        return "redirect:/admin/specialites";
    }

    @GetMapping("/specialites/delete/{id}")
    public String deleteSpecialite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        specialiteService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Spécialité supprimée!");
        return "redirect:/admin/specialites";
    }

    // ===================== GROUPES =====================
    @GetMapping("/groupes")
    public String groupes(Model model) {
        model.addAttribute("groupes", groupeService.findAll());
        return "admin/groupes/list";
    }

    @GetMapping("/groupes/new")
    public String newGroupe(Model model) {
        model.addAttribute("groupe", new Groupe());
        model.addAttribute("sessions", sessionService.findAll());
        return "admin/groupes/form";
    }

    @GetMapping("/groupes/edit/{id}")
    public String editGroupe(@PathVariable Long id, Model model) {
        Groupe groupe = groupeService.findById(id).orElse(null);
        if (groupe == null)
            return "redirect:/admin/groupes";
        model.addAttribute("groupe", groupe);
        model.addAttribute("sessions", sessionService.findAll());
        return "admin/groupes/form";
    }

    @PostMapping("/groupes/save")
    public String saveGroupe(@ModelAttribute Groupe groupe,
            @RequestParam(required = false) Long sessionId,
            RedirectAttributes redirectAttributes) {
        if (sessionId != null) {
            sessionService.findById(sessionId).ifPresent(groupe::setSession);
        }
        groupeService.save(groupe);
        redirectAttributes.addFlashAttribute("success", "Groupe enregistré!");
        return "redirect:/admin/groupes";
    }

    @GetMapping("/groupes/delete/{id}")
    public String deleteGroupe(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        groupeService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Groupe supprimé!");
        return "redirect:/admin/groupes";
    }

    // ===================== SESSIONS PEDAGOGIQUES =====================
    @GetMapping("/sessions")
    public String sessions(Model model) {
        model.addAttribute("sessions", sessionService.findAll());
        return "admin/sessions/list";
    }

    @GetMapping("/sessions/new")
    public String newSession(Model model) {
        model.addAttribute("session", new SessionPedagogique());
        return "admin/sessions/form";
    }

    @GetMapping("/sessions/edit/{id}")
    public String editSession(@PathVariable Long id, Model model) {
        SessionPedagogique session = sessionService.findById(id).orElse(null);
        if (session == null)
            return "redirect:/admin/sessions";
        model.addAttribute("session", session);
        return "admin/sessions/form";
    }

    @PostMapping("/sessions/save")
    public String saveSession(@ModelAttribute SessionPedagogique session, RedirectAttributes redirectAttributes) {
        sessionService.save(session);
        redirectAttributes.addFlashAttribute("success", "Session pédagogique enregistrée!");
        return "redirect:/admin/sessions";
    }

    @GetMapping("/sessions/delete/{id}")
    public String deleteSession(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        sessionService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Session pédagogique supprimée!");
        return "redirect:/admin/sessions";
    }

    @PostMapping("/sessions/{id}/activer")
    public String activerSession(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        sessionService.setActive(id);
        redirectAttributes.addFlashAttribute("success", "Session activée!");
        return "redirect:/admin/sessions";
    }

    // ===================== PLANNING / SEANCES =====================
    @GetMapping("/planning")
    public String planning(Model model) {
        model.addAttribute("seances", seanceService.findAll());
        model.addAttribute("cours", coursService.findAll());
        return "admin/planning/list";
    }

    @GetMapping("/planning/new")
    public String newSeance(Model model) {
        model.addAttribute("seance", new Seance());
        model.addAttribute("cours", coursService.findAll());
        return "admin/planning/form";
    }

    @PostMapping("/planning/save")
    public String saveSeance(@ModelAttribute Seance seance,
            @RequestParam Long coursId,
            RedirectAttributes redirectAttributes) {
        try {
            coursService.findById(coursId).ifPresent(seance::setCours);
            seanceService.save(seance);
            redirectAttributes.addFlashAttribute("success", "Séance planifiée!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/planning";
    }

    @GetMapping("/planning/delete/{id}")
    public String deleteSeance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        seanceService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Séance supprimée!");
        return "redirect:/admin/planning";
    }

    // ===================== STATISTIQUES =====================
    @GetMapping("/statistiques")
    public String statistiques(Model model) {
        // Moyennes par cours
        List<Cours> cours = coursService.findAll();
        Map<Long, Double> moyennesParCours = new HashMap<>();
        Map<Long, Double> tauxReussiteParCours = new HashMap<>();

        for (Cours c : cours) {
            moyennesParCours.put(c.getId(), noteService.calculateMoyenneCours(c.getId()));
            tauxReussiteParCours.put(c.getId(), noteService.getTauxReussite(c.getId(), 10.0));
        }

        // Cours les plus suivis
        List<Map<String, Object>> coursPopulaires = coursService.findCoursLesPlusSuivis(10);

        model.addAttribute("cours", cours);
        model.addAttribute("moyennesParCours", moyennesParCours);
        model.addAttribute("tauxReussiteParCours", tauxReussiteParCours);
        model.addAttribute("coursPopulaires", coursPopulaires);

        return "admin/statistiques";
    }

    @GetMapping("/profil")
    public String profil(Model model, java.security.Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "admin/profil";
    }

    @PostMapping("/profil/update")
    public String updateProfil(@RequestParam String email,
            java.security.Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName()).orElse(null);
            if (user == null)
                return "redirect:/login";

            // Check if new email is already taken by another user
            if (!user.getEmail().equals(email) && userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Cet email est déjà utilisé.");
                return "redirect:/admin/profil";
            }

            user.setEmail(email);
            // Use update method which preserves password if not provided
            userService.update(user.getId(), user);

            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du profil: " + e.getMessage());
        }
        return "redirect:/admin/profil";
    }

    @PostMapping("/profil/password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            java.security.Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas.");
                return "redirect:/admin/profil";
            }

            User user = userService.findByEmail(principal.getName()).orElse(null);
            if (user == null)
                return "redirect:/login";

            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/profil";
    }
}
