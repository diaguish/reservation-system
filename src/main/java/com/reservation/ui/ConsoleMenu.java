package com.reservation.ui;

import com.reservation.factory.ReservationFactory;
import com.reservation.manager.Reservation;
import com.reservation.manager.ReservationManager;
import com.reservation.model.Reservable;
import com.reservation.observer.ConsoleDisplay;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Menu console interactif du système de réservation.
 *
 * SOLID — SRP :
 *   Cette classe a une seule responsabilité : gérer l'interaction
 *   avec l'utilisateur. Elle ne calcule pas de prix, ne crée pas
 *   d'objets directement, ne gère pas la liste des réservations.
 *   Elle délègue tout à ReservationFactory et ReservationManager.
 *
 * SOLID — DIP :
 *   Le menu dépend de ReservationFactory et ReservationManager
 *   (qui eux-mêmes dépendent d'abstractions). Le menu ne connaît
 *   jamais MeetingRoom ou ConferencePricing directement.
 *
 * Design Pattern — Observer :
 *   ConsoleDisplay est enregistré auprès du Manager au démarrage.
 *   Le menu n'a donc pas besoin d'afficher lui-même les confirmations :
 *   c'est l'observateur qui s'en charge automatiquement.
 */
public class ConsoleMenu {

    // --- Constantes d'affichage ---
    private static final String SEPARATEUR =
        "========================================";
    private static final String SEPARATEUR_LEGER =
        "----------------------------------------";
    private static final DateTimeFormatter FORMATTER_SAISIE =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- Dépendances ---
    private final ReservationManager manager;
    private final ReservationFactory factory;
    private final Scanner scanner;

    /**
     * Constructeur : initialise les dépendances et enregistre l'observateur.
     *
     * Pattern Observer :
     *   On enregistre ConsoleDisplay ici, au démarrage de l'application.
     *   À partir de ce moment, toute création ou annulation sera
     *   automatiquement affichée sans que le menu le demande.
     */
    public ConsoleMenu() {
        this.manager = ReservationManager.getInstance();
        this.factory = new ReservationFactory();
        this.scanner = new Scanner(System.in);

        // Enregistrement de l'observateur d'affichage (pattern Observer)
        manager.ajouterObserver(new ConsoleDisplay());
    }

    /**
     * Lance la boucle principale du menu.
     * Tourne jusqu'à ce que l'utilisateur choisisse de quitter.
     */
    public void demarrer() {
        afficherBienvenue();

        boolean continuer = true;
        while (continuer) {
            afficherMenuPrincipal();
            int choix = lireEntier("Votre choix : ", 1, 7);

            switch (choix) {
                case 1 -> creerReservation();
                case 2 -> annulerReservation();
                case 3 -> listerToutesLesReservations();
                case 4 -> chercherParId();
                case 5 -> afficherDetailReservation();
                case 6 -> listerParEntreprise();
                case 7 -> continuer = quitter();
            }
        }
    }

    // =========================================================
    //  AFFICHAGE DU MENU
    // =========================================================

    private void afficherBienvenue() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  Système de réservation de salles");
        System.out.println(SEPARATEUR);
        System.out.println("  Bienvenue ! Utilisez le menu ci-dessous.");
        System.out.println(SEPARATEUR + "\n");
    }

    private void afficherMenuPrincipal() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  MENU PRINCIPAL");
        System.out.println(SEPARATEUR);
        System.out.println("  1. Créer une réservation");
        System.out.println("  2. Annuler une réservation");
        System.out.println("  3. Lister toutes les réservations");
        System.out.println("  4. Chercher une réservation par ID");
        System.out.println("  5. Afficher le détail d'une réservation");
        System.out.println("  6. Lister les réservations par entreprise");
        System.out.println("  7. Quitter");
        System.out.println(SEPARATEUR);
    }

    // =========================================================
    //  FONCTIONNALITÉ 1 — Créer une réservation
    // =========================================================

    /**
     * Guide l'utilisateur pour créer une réservation.
     *
     * Cas limites gérés :
     *   - Type de ressource invalide
     *   - Date dans le passé (géré par la Factory)
     *   - Durée négative ou nulle (géré par la Factory)
     *   - Format de date incorrect (géré par lireDate())
     */
    private void creerReservation() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  CRÉER UNE RÉSERVATION");
        System.out.println(SEPARATEUR);

        // Choix du type de ressource
        System.out.println("  Type de ressource :");
        System.out.println("    1. Salle de réunion");
        System.out.println("    2. Salle de conférence");
        System.out.println("    3. Équipement");
        int typeChoix = lireEntier("Votre choix : ", 1, 3);

        // Récupération de la ressource selon le type choisi
        // Note : cette méthode sera branchée sur les vraies classes
        // du binôme A lors de l'intégration finale
        Reservable ressource = choisirRessource(typeChoix);
        if (ressource == null) {
            System.out.println("  [ERREUR] Aucune ressource disponible pour ce type.");
            return;
        }

        // Saisie de l'entreprise
        String entreprise = lireChaine("  Nom de l'entreprise : ");

        // Saisie de la date de début
        LocalDateTime debut = lireDate("  Date de début (dd/MM/yyyy HH:mm) : ");

        // Saisie de la durée
        String uniteLabel = (typeChoix == 3) ? "jours" : "heures";
        long duree = lireEntier(
            "  Durée en " + uniteLabel + " : ", 1, 720);

        // Création via la Factory (qui valide et délègue au Manager)
        try {
            factory.creer(ressource, debut, duree, entreprise);
            // L'affichage de confirmation est géré par ConsoleDisplay (Observer)
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERREUR] " + e.getMessage());
        }
    }

    /**
     * Retourne une ressource Reservable selon le type choisi.
     *
     * Note d'intégration :
     *   Pour l'instant, cette méthode retourne null car les classes
     *   du binôme A ne sont pas encore disponibles.
     *   Lors de l'intégration, on remplacera chaque case par
     *   l'instanciation réelle (ex: new MeetingRoom(...)).
     *
     *   C'est le seul endroit du menu où on instanciera des classes
     *   concrètes — ce qui respecte le principe Factory du cours.
     */
    private Reservable choisirRessource(int type) {
        // TODO (intégration) : instancier les vraies classes du binôme A
        // Exemple futur :
        // case 1 -> { afficherSallesReunion(); return salleChoisie; }
        // case 2 -> { afficherSallesConference(); return salleChoisie; }
        // case 3 -> { afficherEquipements(); return equipementChoisi; }

        System.out.println("  [INFO] Intégration binôme A en attente.");
        System.out.println("         Les ressources seront disponibles après la merge.");
        return null;
    }

    // =========================================================
    //  FONCTIONNALITÉ 2 — Annuler une réservation
    // =========================================================

    /**
     * Annule une réservation à partir de son ID.
     *
     * Cas limites gérés :
     *   - ID vide ou null
     *   - ID inexistant dans le système
     */
    private void annulerReservation() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  ANNULER UNE RÉSERVATION");
        System.out.println(SEPARATEUR);

        // On affiche d'abord la liste pour aider l'utilisateur
        if (manager.listerReservations().isEmpty()) {
            System.out.println("  Aucune réservation active.");
            return;
        }

        afficherListeResumee();

        String id = lireChaine("  ID de la réservation à annuler : ");

        // Confirmation avant annulation (cas limite UX)
        System.out.print("  Confirmer l'annulation de " + id + " ? (o/n) : ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("o")) {
            System.out.println("  Annulation abandonnée.");
            return;
        }

        boolean succes = manager.annulerReservation(id);
        if (!succes) {
            System.out.println("  [ERREUR] Réservation introuvable : " + id);
        }
        // Si succès, ConsoleDisplay (Observer) affiche la confirmation
    }

    // =========================================================
    //  FONCTIONNALITÉ 3 — Lister toutes les réservations
    // =========================================================

    /**
     * Affiche toutes les réservations avec leur détail complet.
     * Se met à jour automatiquement (l'Observer a déjà notifié
     * les changements en temps réel).
     */
    private void listerToutesLesReservations() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  TOUTES LES RÉSERVATIONS");
        System.out.println(SEPARATEUR);

        List<Reservation> liste = manager.listerReservations();

        // Cas limite : liste vide
        if (liste.isEmpty()) {
            System.out.println("  Aucune réservation enregistrée.");
            return;
        }

        System.out.println("  " + liste.size() + " réservation(s) active(s) :\n");
        for (Reservation r : liste) {
            System.out.println(r.toString());
            System.out.println(SEPARATEUR_LEGER);
        }
    }

    // =========================================================
    //  FONCTIONNALITÉ 4 — Chercher par ID
    // =========================================================

    /**
     * Recherche et affiche une réservation par son ID.
     *
     * Cas limite : ID inexistant → message clair, pas de crash.
     */
    private void chercherParId() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  CHERCHER PAR ID");
        System.out.println(SEPARATEUR);

        String id = lireChaine("  ID de la réservation : ");
        Reservation r = manager.trouverParId(id);

        if (r == null) {
            System.out.println("  [ERREUR] Aucune réservation trouvée pour l'ID : " + id);
        } else {
            System.out.println("\n" + r.toString());
        }
    }

    // =========================================================
    //  FONCTIONNALITÉ 5 — Afficher le détail d'une réservation
    // =========================================================

    /**
     * Affiche le détail complet d'une réservation choisie dans la liste.
     */
    private void afficherDetailReservation() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  DÉTAIL D'UNE RÉSERVATION");
        System.out.println(SEPARATEUR);

        if (manager.listerReservations().isEmpty()) {
            System.out.println("  Aucune réservation active.");
            return;
        }

        afficherListeResumee();
        String id = lireChaine("  ID à afficher : ");
        Reservation r = manager.trouverParId(id);

        if (r == null) {
            System.out.println("  [ERREUR] Réservation introuvable : " + id);
            return;
        }

        System.out.println("\n" + SEPARATEUR);
        System.out.println("  DÉTAIL COMPLET");
        System.out.println(SEPARATEUR);
        System.out.println(r.toString());
        System.out.println("  Description : " + r.getReservable().getDescription());
        System.out.println(SEPARATEUR);
    }

    // =========================================================
    //  FONCTIONNALITÉ 6 — Lister par entreprise
    // =========================================================

    /**
     * Filtre et affiche les réservations d'une entreprise donnée.
     *
     * Cas limite : aucune réservation pour cette entreprise → message clair.
     */
    private void listerParEntreprise() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  RÉSERVATIONS PAR ENTREPRISE");
        System.out.println(SEPARATEUR);

        String entreprise = lireChaine("  Nom de l'entreprise : ");

        // Filtrage via Stream (Java 17)
        List<Reservation> resultats = manager.listerReservations()
                .stream()
                .filter(r -> r.getEntreprise()
                        .equalsIgnoreCase(entreprise.trim()))
                .collect(Collectors.toList());

        // Cas limite : aucun résultat
        if (resultats.isEmpty()) {
            System.out.println("  Aucune réservation pour : " + entreprise);
            return;
        }

        System.out.println("  " + resultats.size()
                + " réservation(s) pour " + entreprise + " :\n");

        for (Reservation r : resultats) {
            System.out.println(r.toString());
            System.out.println(SEPARATEUR_LEGER);
        }
    }

    // =========================================================
    //  FONCTIONNALITÉ 7 — Quitter
    // =========================================================

    /**
     * Demande confirmation avant de quitter.
     * @return false si on quitte, true si on reste
     */
    private boolean quitter() {
        System.out.print("\n  Voulez-vous vraiment quitter ? (o/n) : ");
        String rep = scanner.nextLine().trim().toLowerCase();
        if (rep.equals("o")) {
            System.out.println("\n  Au revoir !\n");
            scanner.close();
            return false; // arrête la boucle
        }
        return true; // continue la boucle
    }

    // =========================================================
    //  UTILITAIRES — Saisie sécurisée
    // =========================================================

    /**
     * Affiche la liste résumée des réservations (ID + nom ressource).
     * Utilisé dans annuler et afficher détail pour guider l'utilisateur.
     */
    private void afficherListeResumee() {
        System.out.println("  Réservations disponibles :");
        for (Reservation r : manager.listerReservations()) {
            System.out.printf("    %-10s | %-20s | %s%n",
                r.getId(),
                r.getReservable().getNom(),
                r.getEntreprise());
        }
        System.out.println();
    }

    /**
     * Lit un entier dans un intervalle [min, max].
     * Boucle jusqu'à obtenir une valeur valide.
     *
     * Cas limites gérés :
     *   - Saisie non numérique → redemande
     *   - Valeur hors intervalle → redemande
     */
    private int lireEntier(String invite, int min, int max) {
        while (true) {
            System.out.print("  " + invite);
            String ligne = scanner.nextLine().trim();
            try {
                int valeur = Integer.parseInt(ligne);
                if (valeur >= min && valeur <= max) return valeur;
                System.out.println("  [ERREUR] Entrez un nombre entre "
                        + min + " et " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [ERREUR] Saisie invalide, entrez un nombre entier.");
            }
        }
    }

    /**
     * Lit une chaîne non vide.
     * Cas limite : chaîne vide ou espaces uniquement → redemande.
     */
    private String lireChaine(String invite) {
        while (true) {
            System.out.print(invite);
            String valeur = scanner.nextLine().trim();
            if (!valeur.isBlank()) return valeur;
            System.out.println("  [ERREUR] La saisie ne peut pas être vide.");
        }
    }

    /**
     * Lit une date au format dd/MM/yyyy HH:mm.
     * Cas limites gérés :
     *   - Format incorrect → redemande avec message clair
     *   - Date dans le passé → avertissement (bloqué ensuite par la Factory)
     */
    private LocalDateTime lireDate(String invite) {
        while (true) {
            System.out.print(invite);
            String saisie = scanner.nextLine().trim();
            try {
                LocalDateTime dt = LocalDateTime.parse(saisie, FORMATTER_SAISIE);
                // Avertissement si date passée (la Factory bloquera de toute façon)
                if (dt.isBefore(LocalDateTime.now())) {
                    System.out.println("  [ATTENTION] La date est dans le passé.");
                }
                return dt;
            } catch (DateTimeParseException e) {
                System.out.println(
                    "  [ERREUR] Format invalide. Utilisez : dd/MM/yyyy HH:mm");
                System.out.println("  Exemple : 25/06/2025 14:30");
            }
        }
    }
}