package spring.miniprojet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import spring.miniprojet.service.CoursService;
import spring.miniprojet.service.EtudiantService;
import spring.miniprojet.service.NoteService;

@Controller
@RequestMapping("/admin/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final EtudiantService etudiantService;
    private final CoursService coursService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notes", noteService.findAll());
        return "admin/notes/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("etudiants", etudiantService.findAll());
        model.addAttribute("cours", coursService.findAll());
        return "admin/notes/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam Long etudiantId, @RequestParam Long coursId,
            @RequestParam Double valeur, @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {
        try {
            noteService.attribuerNote(etudiantId, coursId, valeur, commentaire);
            redirectAttributes.addFlashAttribute("success", "Note attribuée avec succès");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/notes";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        var note = noteService.findById(id).orElseThrow();
        model.addAttribute("note", note);
        return "admin/notes/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @RequestParam Double valeur,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {
        noteService.update(id, valeur, commentaire);
        redirectAttributes.addFlashAttribute("success", "Note modifiée avec succès");
        return "redirect:/admin/notes";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        noteService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Note supprimée");
        return "redirect:/admin/notes";
    }

    @GetMapping("/etudiant/{id}")
    public String notesByEtudiant(@PathVariable Long id, Model model) {
        model.addAttribute("etudiant", etudiantService.findById(id).orElseThrow());
        model.addAttribute("notes", noteService.findByEtudiantId(id));
        model.addAttribute("moyenne", noteService.calculateMoyenneEtudiant(id));
        return "admin/notes/etudiant";
    }

    @GetMapping("/cours/{id}")
    public String notesByCours(@PathVariable Long id, Model model) {
        model.addAttribute("cours", coursService.findById(id).orElseThrow());
        model.addAttribute("notes", noteService.findByCoursId(id));
        model.addAttribute("moyenne", noteService.calculateMoyenneCours(id));
        model.addAttribute("tauxReussite", noteService.getTauxReussite(id, 10.0));
        return "admin/notes/cours";
    }
}
