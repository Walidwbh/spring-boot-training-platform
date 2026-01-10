# Documentation Fonctionnelle et Logique Métier - Mini-Projet Gestion Scolaire

Ce document décrit en détail la logique métier, les fonctionnalités couvertes par l'application, les rôles des utilisateurs ainsi que leurs limites. L'application vise à digitaliser la gestion d'une école d'ingénieurs.

## 1. Vue d'ensemble des Acteurs et Rôles

Le système met en œuvre une sécurité basée sur les rôles (RBAC) avec trois niveaux d'accès distincts :

1.  **ADMIN** : Administrateur système et pédagogique.
2.  **FORMATEUR** : Enseignant responsable de modules.
3.  **ETUDIANT** : Apprenant utilisateur de la plateforme.

---

## 2. Fonctionnalités et Règles par Rôle

### 👑 Administrateur (ADMIN)

L'administrateur possède un contrôle total sur le système. Il est le garant de la structure pédagogique.

**Fonctionnalités :**
*   **Gestion des Utilisateurs** : Création, modification et suppression des comptes Étudiants et Formateurs.
*   **Gestion Pédagogique** :
    *   Définition des **Spécialités** (Informatique, IA, Réseaux, etc.).
    *   Gestion des **Sessions Pédagogiques** (Année universitaire, Semestres).
    *   Gestion des **Groupes d'étudiants** (TP, TD, Cours Magistraux).
*   **Gestion des Cours** :
    *   Création de cours et affectation à un Formateur.
    *   Liaison des cours à une spécialité et une session.
    *   **Affectation des Groupes** à des cours (permettant la gestion des emplois du temps groupés).
*   **Gestion des Inscriptions** :
    *   Validation ou Refus des demandes d'inscription faites par les étudiants (Workflow : `EN_ATTENTE` ➔ `VALIDEE` ou `REFUSEE`).
*   **Tableau de Bord** : Vue globale avec statistiques (inscriptions en attente, cours populaires, etc.).

**Limites :**
*   L'administrateur ne saisit généralement pas les notes (bien qu'il puisse techniquement avoir les droits pour corriger, c'est le rôle du formateur).

### 🎓 Formateur

Le formateur gère le côté opérationnel de ses enseignements.

**Fonctionnalités :**
*   **Mes Cours** : Consultation uniquement des cours qui lui sont assignés par l'administrateur.
*   **Gestion des Notes** :
    *   Saisie des notes pour les étudiants inscrits à ses cours.
    *   Modification des notes et ajout de commentaires/appréciations.
*   **Planification (Emploi du temps)** :
    *   Planification des séances de cours (Date, Heure de début, Heure de fin, Salle, Type : Cours/TD/TP/Examen).
    *   Le système **bloque automatiquement** la création d'une séance s'il y a conflit (voir Règles de Gestion).
    *   Consultation de son emploi du temps hebdomadaire.
*   **Suivi des Étudiants** : Visualisation des listes d'émargement et des inscrits.
*   **Profil** : Mise à jour de ses informations personnelles (spécialité, téléphone).

**Limites :**
*   Ne peut modifier que ses propres cours.
*   Ne peut pas inscrire directement un étudiant (l'étudiant doit faire la demande ou l'admin l'inscrit).
*   Ne peut pas créer de nouveaux cours ou spécialités.

### 🎒 Étudiant

L'étudiant est consommateur de l'information.

**Fonctionnalités :**
*   **Catalogue des Cours** : Consultation des cours disponibles.
*   **Inscription** : Possibilité de demander l'inscription à un cours (sujet à validation).
*   **Mes Cours** : Accès aux détails des cours où il est inscrit (validé).
*   **Notes & Résultats** :
    *   Consultation de ses notes par matière.
    *   Visualisation de sa moyenne générale.
    *   Téléchargement du relevé de notes au format PDF.
*   **Emploi du Temps** :
    *   Visualisation de son planning personnel (basé sur les inscriptions aux cours et l'appartenance aux groupes).
*   **Profil** : Gestion de ses données et changement de mot de passe.

**Limites :**
*   Accès en lecture seule sur la majorité des données académiques.
*   Ne peut pas voir les notes des autres étudiants.

---

## 3. Logique Métier et Règles de Gestion Avancées

C'est ici que réside "l'intelligence" de l'application implémentée dans la couche Service.

### 🛡️ Gestion des Conflits d'Horaires (Séances)
L'application implémente une vérification stricte lors de la planification d'une séance par un formateur.
Une séance **ne peut pas** être créée si :
1.  **Conflit Formateur** : Le formateur donne déjà un autre cours sur ce créneau horaire.
2.  **Conflit Salle** : La salle sélectionnée est déjà occupée par une autre séance (tous cours confondus).
3.  **Conflit Étudiants/Groupes** : Un ou plusieurs **groupes d'étudiants** associés au cours sont déjà occupés par une autre séance (d'une autre matière) sur ce même créneau. Cela garantit qu'un étudiant n'a pas deux cours en même temps.

### 📝 Workflow d'Inscription
1.  L'étudiant demande à s'inscrire à un cours via son interface.
2.  Une inscription est créée avec le statut **`EN_ATTENTE`**.
3.  L'administrateur voit la demande sur son tableau de bord.
4.  L'administrateur valide l'inscription ➔ le statut passe à **`CONFIRMEE`**.
    *   Seuls les étudiants avec une inscription `CONFIRMEE` apparaissent dans les listes du formateur pour la notation.

### 📊 Calcul des Notes et Moyennes
*   Les notes sont attribuées sur 20.
*   La moyenne générale d'un étudiant est calculée dynamiquement en se basant sur toutes les notes enregistrées.
*   Un système d'appréciation automatique (Excellent, Très bien, Passable, etc.) est affiché côté étudiant selon la moyenne.

### 🔒 Sécurité et Données
*   Les mots de passe sont hachés (BCrypt) avant stockage.
*   L'accès aux API et aux pages Web est cloisonné par les rôles (Interdiction pour un étudiant d'accéder aux URL `/admin/...` ou `/formateur/...`).

## 4. Architecture Technique

*   **Backend** : Spring Boot 3 (Java).
*   **Base de données** : Relationnelle (JPA/Hibernate).
*   **Frontend (SSR)** : Thymeleaf pour le rendu des pages HTML côté serveur.
*   **Frontend (CSR)** : Une API REST complète est exposée sous `/api/...` pour permettre le développement futur d'applications mobiles ou Single Page Applications (React/Angular).
*   **Reporting** : Génération de PDF via iText.
