package com.reservation.ui;

import com.reservation.factory.ReservationFactory;
import com.reservation.manager.Reservation;
import com.reservation.manager.ReservationManager;
import com.reservation.model.Reservable;
import com.reservation.model.impl.SportCourt;
import com.reservation.model.impl.SportPricing;
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
 *   les classes concrètes qu'à un seul endroit : choisirRessource(),
 *   et uniquement pour SportCourt qui est sa responsabilité directe.
 *   Les cases 1, 2, 3 seront complétés lors de la merge avec le binôme A.
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
     *   - Ressource indisponible sur le créneau (IllegalStateException)
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
        System.out.println("    4. Terrain de sport");
        int typeChoix = lireEntier("  Votre choix : ", 1, 4);

        // Récupération de la ressource selon le type choisi
        Reservable ressource = choisirRessource(typeChoix);
        if (ressource == null) {
            System.out.println("  [ERREUR] Aucune ressource disponible pour ce type.");
            return;
        }

        // Saisie de l'entreprise
        String entreprise = lireChaine("  Nom de l'entreprise : ");

        // Saisie de la date de début
        LocalDateTime debut = lireDate("  Date de début (dd/MM/yyyy HH:mm) : ");

        // Terrain et équipement se réservent en jours, les salles en heures
        String uniteLabel = (typeChoix == 3 || typeChoix == 4) ? "jours" : "heures";
        long duree = lireEntier("  Durée en " + uniteLabel + " : ", 1, 720);

        // Création via la Factory (valide et délègue au Manager)
        // Le Manager vérifie la disponibilité et bloque les créneaux
        // L'affichage de confirmation est géré par ConsoleDisplay (Observer)
        try {
            factory.creer(ressource, debut, duree, entreprise);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // IllegalArgumentException : paramètres invalides (Factory)
            // IllegalStateException    : créneau indisponible (Manager)
            System.out.println("  [ERREUR] " + e.getMessage());
        }
    }

    /**
     * Retourne une ressource Reservable selon le type choisi.
     *
     * C'est le seul endroit du menu qui instancie des classes concrètes.
     * Cases 1, 2, 3 : complétés après la merge avec le binôme A.
     * Case 4 : SportCourt disponible dès maintenant.
     *
     * SOLID — DIP respecté :
     *   La valeur de retour est toujours typée Reservable (abstraction).
     *   Le reste du menu ne sait pas ce qu'il y a dedans.
     */
    private Reservable choisirRessource(int type) {
        switch (type) {
            case 1 -> {
                // TODO (merge binôme A) : instancier MeetingRoom
                System.out.println("  [INFO] Intégration MeetingRoom en attente.");
                return null;
            }
            case 2 -> {
                // TODO (merge binôme A) : instancier ConferenceRoom
                System.out.println("  [INFO] Intégration ConferenceRoom en attente.");
                return null;
            }
            case 3 -> {
                // TODO (merge binôme A) : instancier Equipment
                System.out.println("  [INFO] Intégration Equipment en attente.");
                return null;
            }
            case 4 -> {
                return creerTerrain();
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Guide la création d'un terrain de sport.
     *
     * Extrait dans sa propre méthode pour respecter SRP :
     *   choisirRessource() reste lisible et chaque type
     *   de ressource aura sa propre méthode de création.
     *
     * La météo est demandée ICI avant la création car elle influence
     * directement le calcul du prix via SportPricing.setMauvaisTemps().
     *
     * @return le SportCourt configuré, ou null si saisie invalide
     */
    private Reservable creerTerrain() {
        System.out.println("\n" + SEPARATEUR_LEGER);
        System.out.println("  TERRAIN DE SPORT");
        System.out.println(SEPARATEUR_LEGER);

        // Identité du terrain
        String idTerrain  = lireChaine("  ID du terrain (ex: SPORT-01) : ");
        String nomTerrain = lireChaine("  Nom du terrain : ");

        // Paramètres tarifaires
        System.out.print("  Prix de base par jour (€) : ");
        double baseSport = lireDouble(1.0, 10000.0);

        System.out.print("  Réduction si mauvais temps (ex: 0.20 pour 20%) : ");
        double reduction = lireDouble(0.0, 1.0);

        // Condition météo — influence le prix calculé
        System.out.print("  Mauvais temps prévu ? (pluie/neige) (o/n) : ");
        boolean pluie = scanner.nextLine().trim().equalsIgnoreCase("o");

        // Construction du terrain avec sa stratégie de prix
        SportPricing sp      = new SportPricing(baseSport, reduction);
        SportCourt   terrain = new SportCourt(idTerrain, nomTerrain, sp);
        terrain.setMauvaisTemps(pluie);

        // Récapitulatif avant confirmation
        System.out.println(SEPARATEUR_LEGER);
        System.out.printf("  Terrain   : %s (%s)%n", nomTerrain, idTerrain);
        System.out.printf("  Base/jour : %.2f €%n", baseSport);
        System.out.printf("  Réduction : %.0f%%%n", reduction * 100);
        if (pluie) {
            System.out.printf("  Prix effectif : %.2f €/jour (réduction appliquée)%n",
                baseSport * (1 - reduction));
        } else {
            System.out.printf("  Prix effectif : %.2f €/jour (pas de réduction)%n",
                baseSport);
        }
        System.out.println(SEPARATEUR_LEGER);

        return terrain;
    }

    // =========================================================
    //  FONCTIONNALITÉ 2 — Annuler une réservation
    // =========================================================

    /**
     * Annule une réservation à partir de son ID.
     *
     * Cas limites gérés :
     *   - Liste vide → message et retour immédiat
     *   - ID vide ou null → géré par lireChaine()
     *   - ID inexistant → message clair
     *   - Annulation refusée par l'utilisateur → abandon propre
     */
    private void annulerReservation() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  ANNULER UNE RÉSERVATION");
        System.out.println(SEPARATEUR);

        if (manager.listerReservations().isEmpty()) {
            System.out.println("  Aucune réservation active.");
            return;
        }

        // Affichage de la liste pour guider le choix
        afficherListeResumee();

        String id = lireChaine("  ID de la réservation à annuler : ");

        // Confirmation avant action irréversible
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
        // Si succès : ConsoleDisplay (Observer) affiche la confirmation
        // et les créneaux sont libérés dans DisponibiliteManager
    }

    // =========================================================
    //  FONCTIONNALITÉ 3 — Lister toutes les réservations
    // =========================================================

    /**
     * Affiche toutes les réservations actives avec leur détail complet.
     *
     * Cas limite : liste vide → message clair.
     */
    private void listerToutesLesReservations() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  TOUTES LES RÉSERVATIONS");
        System.out.println(SEPARATEUR);

        List<Reservation> liste = manager.listerReservations();

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
            System.out.println("  [ERREUR] Aucune réservation trouvée pour : " + id);
        } else {
            System.out.println("\n" + r.toString());
        }
    }

    // =========================================================
    //  FONCTIONNALITÉ 5 — Afficher le détail d'une réservation
    // =========================================================

    /**
     * Affiche le détail complet d'une réservation sélectionnée.
     *
     * Cas limites :
     *   - Liste vide → retour immédiat
     *   - ID introuvable → message clair
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
     * Cas limite : aucun résultat → message clair.
     */
    private void listerParEntreprise() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  RÉSERVATIONS PAR ENTREPRISE");
        System.out.println(SEPARATEUR);

        String entreprise = lireChaine("  Nom de l'entreprise : ");

        List<Reservation> resultats = manager.listerReservations()
                .stream()
                .filter(r -> r.getEntreprise()
                        .equalsIgnoreCase(entreprise.trim()))
                .collect(Collectors.toList());

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
     * Demande confirmation avant de quitter proprement.
     *
     * @return false pour arrêter la boucle, true pour continuer
     */
    private boolean quitter() {
        System.out.print("\n  Voulez-vous vraiment quitter ? (o/n) : ");
        String rep = scanner.nextLine().trim().toLowerCase();
        if (rep.equals("o")) {
            System.out.println("\n  Au revoir !\n");
            scanner.close();
            return false;
        }
        return true;
    }

    // =========================================================
    //  UTILITAIRES — Saisie sécurisée
    // =========================================================

    /**
     * Affiche la liste résumée : ID | nom ressource | entreprise.
     * Utilisé avant annuler et afficher détail pour guider l'utilisateur.
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
     * Lit un entier dans [min, max] avec boucle de validation.
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
     * Lit un double dans [min, max] avec boucle de validation.
     * Utilisé pour les paramètres tarifaires du terrain de sport.
     *
     * Cas limites gérés :
     *   - Saisie non numérique → redemande
     *   - Valeur hors intervalle → redemande
     */
    private double lireDouble(double min, double max) {
        while (true) {
            String ligne = scanner.nextLine().trim();
            try {
                double valeur = Double.parseDouble(ligne);
                if (valeur >= min && valeur <= max) return valeur;
                System.out.printf("  [ERREUR] Entrez une valeur entre %.2f et %.2f : ",
                    min, max);
            } catch (NumberFormatException e) {
                System.out.print("  [ERREUR] Saisie invalide, entrez un nombre : ");
            }
        }
    }

    /**
     * Lit une chaîne non vide avec boucle de validation.
     *
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
     * Lit une date au format dd/MM/yyyy HH:mm avec boucle de validation.
     *
     * Cas limites gérés :
     *   - Format incorrect → redemande avec exemple
     *   - Date dans le passé → avertissement (bloqué ensuite par la Factory)
     */
    private LocalDateTime lireDate(String invite) {
        while (true) {
            System.out.print(invite);
            String saisie = scanner.nextLine().trim();
            try {
                LocalDateTime dt = LocalDateTime.parse(saisie, FORMATTER_SAISIE);
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