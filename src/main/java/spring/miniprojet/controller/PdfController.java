package spring.miniprojet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.miniprojet.entity.Etudiant;
import spring.miniprojet.entity.User;
import spring.miniprojet.service.EtudiantService;
import spring.miniprojet.service.PdfService;
import spring.miniprojet.service.UserService;

import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final EtudiantService etudiantService;
    private final UserService userService;

    @GetMapping("/releve-notes/{etudiantId}")
    public ResponseEntity<byte[]> releveNotes(@PathVariable Long etudiantId) {
        ByteArrayOutputStream baos = pdfService.generateReleveNotes(etudiantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=releve_notes_" + etudiantId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/mon-releve")
    public ResponseEntity<byte[]> monReleveNotes(Authentication auth) {
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Etudiant etudiant = etudiantService.findByUserId(user.getId()).orElse(null);
        if (etudiant == null) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream baos = pdfService.generateReleveNotes(etudiant.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mon_releve_notes.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/rapport-cours/{coursId}")
    public ResponseEntity<byte[]> rapportCours(@PathVariable Long coursId) {
        ByteArrayOutputStream baos = pdfService.generateRapportCours(coursId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport_cours_" + coursId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/liste-etudiants")
    public ResponseEntity<byte[]> listeEtudiants() {
        ByteArrayOutputStream baos = pdfService.generateListeEtudiants();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=liste_etudiants.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/emploi-du-temps/{etudiantId}")
    public ResponseEntity<byte[]> emploiDuTemps(@PathVariable Long etudiantId,
            @RequestParam String dateDebut,
            @RequestParam String dateFin) {
        ByteArrayOutputStream baos = pdfService.generateEmploiDuTemps(etudiantId, dateDebut, dateFin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=emploi_du_temps_" + etudiantId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
}
