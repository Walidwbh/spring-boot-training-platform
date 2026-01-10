package spring.miniprojet.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.miniprojet.entity.*;
import spring.miniprojet.service.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final EtudiantService etudiantService;
    private final CoursService coursService;
    private final NoteService noteService;
    private final SeanceService seanceService;
    private final InscriptionService inscriptionService;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(78, 115, 223);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(78, 115, 223);

    @Override
    public ByteArrayOutputStream generateReleveNotes(Long etudiantId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Etudiant etudiant = etudiantService.findById(etudiantId).orElse(null);
            if (etudiant == null) {
                document.close();
                return baos;
            }

            // En-tête
            addHeader(document, "RELEVÉ DE NOTES");

            // Informations étudiant
            document.add(new Paragraph("Informations de l'étudiant")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20));

            Table infoTable = new Table(2);
            infoTable.setWidth(UnitValue.createPercentValue(50));
            addInfoRow(infoTable, "Matricule:", etudiant.getMatricule());
            addInfoRow(infoTable, "Nom:", etudiant.getNom() + " " + etudiant.getPrenom());
            addInfoRow(infoTable, "Email:", etudiant.getEmail());
            addInfoRow(infoTable, "Groupe:",
                    etudiant.getGroupe() != null ? etudiant.getGroupe().getNom() : "Non assigné");
            document.add(infoTable);

            // Notes
            document.add(new Paragraph("Notes obtenues")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20));

            List<Note> notes = noteService.findByEtudiantId(etudiantId);

            Table notesTable = new Table(new float[] { 3, 1, 2, 3 });
            notesTable.setWidth(UnitValue.createPercentValue(100));

            // En-têtes
            addTableHeader(notesTable, "Cours", "Note", "Date", "Commentaire");

            // Données
            for (Note note : notes) {
                notesTable.addCell(new Cell().add(new Paragraph(note.getCours().getTitre())));
                notesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f/20", note.getValeur())))
                        .setTextAlignment(TextAlignment.CENTER));
                notesTable.addCell(new Cell().add(new Paragraph(note.getDateSaisie().toString())));
                notesTable.addCell(
                        new Cell().add(new Paragraph(note.getCommentaire() != null ? note.getCommentaire() : "")));
            }

            document.add(notesTable);

            // Moyenne générale
            Double moyenne = etudiantService.calculateMoyenneGenerale(etudiantId);
            document.add(
                    new Paragraph("Moyenne Générale: " + (moyenne != null ? String.format("%.2f/20", moyenne) : "N/A"))
                            .setFontSize(16)
                            .setBold()
                            .setMarginTop(20)
                            .setTextAlignment(TextAlignment.RIGHT));

            // Pied de page
            addFooter(document);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos;
    }

    @Override
    public ByteArrayOutputStream generateRapportCours(Long coursId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Cours cours = coursService.findById(coursId).orElse(null);
            if (cours == null) {
                document.close();
                return baos;
            }

            // En-tête
            addHeader(document, "RAPPORT DE COURS");

            // Informations cours
            document.add(new Paragraph("Informations du cours")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20));

            Table infoTable = new Table(2);
            infoTable.setWidth(UnitValue.createPercentValue(50));
            addInfoRow(infoTable, "Code:", cours.getCode());
            addInfoRow(infoTable, "Titre:", cours.getTitre());
            addInfoRow(infoTable, "Crédits:", String.valueOf(cours.getCredits()));
            addInfoRow(infoTable, "Formateur:",
                    cours.getFormateur() != null
                            ? cours.getFormateur().getNom() + " " + cours.getFormateur().getPrenom()
                            : "Non assigné");
            document.add(infoTable);

            // Statistiques
            document.add(new Paragraph("Statistiques")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20));

            Long nbInscrits = coursService.countEtudiantsInscrits(coursId);
            Double moyenne = noteService.calculateMoyenneCours(coursId);
            Double tauxReussite = noteService.getTauxReussite(coursId, 10.0);

            Table statsTable = new Table(2);
            statsTable.setWidth(UnitValue.createPercentValue(50));
            addInfoRow(statsTable, "Étudiants inscrits:", String.valueOf(nbInscrits));
            addInfoRow(statsTable, "Moyenne du cours:", moyenne != null ? String.format("%.2f/20", moyenne) : "N/A");
            addInfoRow(statsTable, "Taux de réussite:",
                    tauxReussite != null ? String.format("%.1f%%", tauxReussite) : "N/A");
            document.add(statsTable);

            // Liste des notes
            document.add(new Paragraph("Notes des étudiants")
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20));

            List<Note> notes = noteService.findByCoursId(coursId);

            Table notesTable = new Table(new float[] { 2, 3, 1, 2 });
            notesTable.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(notesTable, "Matricule", "Étudiant", "Note", "Résultat");

            for (Note note : notes) {
                notesTable.addCell(new Cell().add(new Paragraph(note.getEtudiant().getMatricule())));
                notesTable.addCell(new Cell()
                        .add(new Paragraph(note.getEtudiant().getNom() + " " + note.getEtudiant().getPrenom())));
                notesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", note.getValeur())))
                        .setTextAlignment(TextAlignment.CENTER));
                String resultat = note.getValeur() >= 10 ? "Admis" : "Ajourné";
                notesTable.addCell(new Cell().add(new Paragraph(resultat))
                        .setFontColor(
                                note.getValeur() >= 10 ? new DeviceRgb(28, 200, 138) : new DeviceRgb(231, 74, 59)));
            }

            document.add(notesTable);

            addFooter(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos;
    }

    @Override
    public ByteArrayOutputStream generateListeEtudiants() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addHeader(document, "LISTE DES ÉTUDIANTS");

            List<Etudiant> etudiants = etudiantService.findAll();

            Table table = new Table(new float[] { 1, 2, 3, 3, 2 });
            table.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(table, "Matricule", "Nom", "Prénom", "Email", "Groupe");

            for (Etudiant e : etudiants) {
                table.addCell(new Cell().add(new Paragraph(e.getMatricule())));
                table.addCell(new Cell().add(new Paragraph(e.getNom())));
                table.addCell(new Cell().add(new Paragraph(e.getPrenom())));
                table.addCell(new Cell().add(new Paragraph(e.getEmail())));
                table.addCell(new Cell().add(new Paragraph(e.getGroupe() != null ? e.getGroupe().getNom() : "-")));
            }

            document.add(table);

            document.add(new Paragraph("Total: " + etudiants.size() + " étudiants")
                    .setMarginTop(10)
                    .setBold());

            addFooter(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos;
    }

    @Override
    public ByteArrayOutputStream generateEmploiDuTemps(Long etudiantId, String dateDebut, String dateFin) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Etudiant etudiant = etudiantService.findById(etudiantId).orElse(null);
            if (etudiant == null) {
                document.close();
                return baos;
            }

            addHeader(document, "EMPLOI DU TEMPS");

            document.add(new Paragraph("Étudiant: " + etudiant.getNom() + " " + etudiant.getPrenom())
                    .setFontSize(12)
                    .setMarginTop(10));
            document.add(new Paragraph("Période: " + dateDebut + " au " + dateFin)
                    .setFontSize(12));

            LocalDate start = LocalDate.parse(dateDebut);
            LocalDate end = LocalDate.parse(dateFin);
            List<Seance> seances = seanceService.findEmploiDuTempsEtudiant(etudiantId, start, end);

            Table table = new Table(new float[] { 2, 2, 3, 2, 2 });
            table.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(table, "Date", "Horaire", "Cours", "Salle", "Type");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Seance s : seances) {
                table.addCell(new Cell().add(new Paragraph(s.getDateSeance().format(dateFormatter))));
                table.addCell(new Cell().add(new Paragraph(s.getHeureDebut() + " - " + s.getHeureFin())));
                table.addCell(new Cell().add(new Paragraph(s.getCours().getTitre())));
                table.addCell(new Cell().add(new Paragraph(s.getSalle())));
                table.addCell(new Cell().add(new Paragraph(s.getType().toString())));
            }

            document.add(table);

            addFooter(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos;
    }

    // ========== Helper Methods ==========

    private void addHeader(Document document, String title) {
        document.add(new Paragraph("CENTRE DE FORMATION")
                .setFontSize(20)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        document.add(
                new Paragraph("Date d'édition: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Document généré automatiquement - Centre de Formation © 2026")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
    }

    private void addTableHeader(Table table, String... headers) {
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }
    }

    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBorder(null));
        table.addCell(new Cell().add(new Paragraph(value)).setBorder(null));
    }
}
