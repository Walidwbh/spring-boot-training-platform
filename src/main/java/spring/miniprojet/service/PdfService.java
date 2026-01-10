package spring.miniprojet.service;

import spring.miniprojet.entity.Etudiant;
import spring.miniprojet.entity.Cours;

import java.io.ByteArrayOutputStream;

public interface PdfService {
    ByteArrayOutputStream generateReleveNotes(Long etudiantId);

    ByteArrayOutputStream generateRapportCours(Long coursId);

    ByteArrayOutputStream generateListeEtudiants();

    ByteArrayOutputStream generateEmploiDuTemps(Long etudiantId, String dateDebut, String dateFin);
}
