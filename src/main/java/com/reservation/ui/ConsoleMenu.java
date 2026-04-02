package com.reservation.ui;

import com.reservation.factory.ReservationFactory;
import com.reservation.manager.Reservation;
import com.reservation.manager.ReservationManager;
import com.reservation.model.Reservable;
import com.reservation.model.impl.ConferencePricing;
import com.reservation.model.impl.ConferenceRoom;
import com.reservation.model.impl.Equipment;
import com.reservation.model.impl.EquipmentPricing;
import com.reservation.model.impl.MeetingPricing;
import com.reservation.model.impl.MeetingRoom;
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
 *   (qui eux-mêmes dépendent d'abstractions).
 *   Les classes concrètes ne sont instanciées qu'à un seul endroit :
 *   les méthodes creerSalleReunion(), creerSalleConference(),
 *   creerEquipement(), creerTerrain(). Partout ailleurs, on
 *   manipule uniquement Reservable (abstraction).
 *
 * Design Pattern — Observer :
 *   ConsoleDisplay est enregistré auprès du Manager au démarrage.
 *   Le menu n'affiche pas lui-même les confirmations :
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
     *   - Ressource indisponible sur le créneau (IllegalStateException)
     *   - Date dans le passé (géré par la Factory)
     *   - Durée négative ou nulle (géré par la Factory)
     *   - Format de date incorrect (géré par lireDate())
     */
    private void creerReservation() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  CRÉER UNE RÉSERVATION");
        System.out.println(SEPARATEUR);

        System.out.println("  Type de ressource :");
        System.out.println("    1. Salle de réunion");
        System.out.println("    2. Salle de conférence");
        System.out.println("    3. Équipement");
        System.out.println("    4. Terrain de sport");
        int typeChoix = lireEntier("  Votre choix : ", 1, 4);

        Reservable ressource = choisirRessource(typeChoix);
        if (ressource == null) {
            System.out.println("  [ERREUR] Création annulée.");
            return;
        }

        String entreprise = lireChaine("  Nom de l'entreprise : ");
        LocalDateTime debut = lireDate("  Date de début (dd/MM/yyyy HH:mm) : ");

        // Équipement et terrain se réservent en jours, les salles en heures
        String uniteLabel = (typeChoix == 3 || typeChoix == 4) ? "jours" : "heures";
        long duree = lireEntier("  Durée en " + uniteLabel + " : ", 1, 720);

        try {
            factory.creer(ressource, debut, duree, entreprise);
            // Confirmation affichée par ConsoleDisplay (Observer)
        } catch (IllegalArgumentException | IllegalStateException e) {
            // IllegalArgumentException : paramètres invalides (Factory)
            // IllegalStateException    : créneau indisponible (Manager)
            System.out.println("  [ERREUR] " + e.getMessage());
        }
    }

    /**
     * Aiguille vers la bonne méthode de création selon le type.
     *
     * C'est le seul endroit du menu qui dispatche vers les classes
     * concrètes. Chaque type a sa propre méthode dédiée pour
     * respecter le SRP — choisirRessource() ne fait que router.
     *
     * La valeur de retour est toujours Reservable (abstraction) :
     * le reste du menu ne sait jamais ce qu'il y a dedans.
     */
    private Reservable choisirRessource(int type) {
        return switch (type) {
            case 1 -> creerSalleReunion();
            case 2 -> creerSalleConference();
            case 3 -> creerEquipement();
            case 4 -> creerTerrain();
            default -> null;
        };
    }

    // =========================================================
    //  CRÉATION DES RESSOURCES — une méthode par type
    // =========================================================

    /**
     * Guide la création d'une salle de réunion.
     *
     * Paramètres demandés à l'utilisateur :
     *   - ID et nom de la salle
     *   - Capacité en personnes
     *   - Base horaire et facteur (utilisés par MeetingPricing)
     *
     * Formule rappelée à l'utilisateur :
     *   prixHoraire = baseReunion + (capacite * facteur)
     *   prix total  = prixHoraire * duree
     */
    private Reservable creerSalleReunion() {
        System.out.println("\n" + SEPARATEUR_LEGER);
        System.out.println("  SALLE DE RÉUNION");
        System.out.println(SEPARATEUR_LEGER);

        String id  = lireChaine("  ID de la salle (ex: MEET-01) : ");
        String nom = lireChaine("  Nom de la salle : ");

        System.out.print("  Capacité (personnes) : ");
        int capacite = (int) lireDouble(1, 500);

        System.out.println("  Tarification : prixHoraire = base + (capacité * facteur)");
        System.out.print("  Base horaire (€) : ");
        double base = lireDouble(0, 10000);

        System.out.print("  Facteur par personne (€) : ");
        double facteur = lireDouble(0, 1000);

        // Récapitulatif avant de retourner la ressource
        double prixHoraire = base + (capacite * facteur);
        System.out.println(SEPARATEUR_LEGER);
        System.out.printf("  Salle     : %s (%s)%n", nom, id);
        System.out.printf("  Capacité  : %d personnes%n", capacite);
        System.out.printf("  Prix/heure: %.2f € (base %.2f + %d×%.2f)%n",
            prixHoraire, base, capacite, facteur);
        System.out.println(SEPARATEUR_LEGER);

        return new MeetingRoom(id, nom, capacite, base, facteur, new MeetingPricing());
    }

    /**
     * Guide la création d'une salle de conférence.
     *
     * Paramètres demandés :
     *   - ID et nom
     *   - Prix du forfait de base
     *   - Durée incluse dans le forfait (heures)
     *   - Supplément par heure si dépassement
     *
     * Formule rappelée :
     *   prix = baseConference
     *   si duree > dureeFixe → prix += (duree - dureeFixe) * extraParHeure
     */
    private Reservable creerSalleConference() {
        System.out.println("\n" + SEPARATEUR_LEGER);
        System.out.println("  SALLE DE CONFÉRENCE");
        System.out.println(SEPARATEUR_LEGER);

        String id  = lireChaine("  ID de la salle (ex: CONF-01) : ");
        String nom = lireChaine("  Nom de la salle : ");

        System.out.print("  Prix forfait de base (€) : ");
        double base = lireDouble(0, 100000);

        System.out.print("  Durée incluse dans le forfait (heures) : ");
        long dureeFixe = (long) lireDouble(1, 720);

        System.out.print("  Supplément par heure de dépassement (€) : ");
        double extra = lireDouble(0, 10000);

        System.out.println(SEPARATEUR_LEGER);
        System.out.printf("  Salle    : %s (%s)%n", nom, id);
        System.out.printf("  Forfait  : %.2f€ pour %dh%n", base, dureeFixe);
        System.out.printf("  Dépass.  : +%.2f€/h au-delà de %dh%n", extra, dureeFixe);
        System.out.println(SEPARATEUR_LEGER);

        return new ConferenceRoom(id, nom, base, dureeFixe, extra,
                                  new ConferencePricing());
    }

    /**
     * Guide la création d'un équipement.
     *
     * Paramètres demandés :
     *   - ID et nom
     *   - Taux journalier
     *   - Pénalité en cas de retard de restitution
     *   - Retard déjà constaté ? (cas limite : équipement rendu en retard)
     *
     * Formule rappelée :
     *   prix = tauxJournalier * jours
     *   si retard → prix += penalite
     */
    private Reservable creerEquipement() {
        System.out.println("\n" + SEPARATEUR_LEGER);
        System.out.println("  ÉQUIPEMENT");
        System.out.println(SEPARATEUR_LEGER);

        String id  = lireChaine("  ID de l'équipement (ex: EQUIP-01) : ");
        String nom = lireChaine("  Nom de l'équipement : ");

        System.out.print("  Taux journalier (€/jour) : ");
        double taux = lireDouble(0, 10000);

        System.out.print("  Pénalité retard (€) : ");
        double penalite = lireDouble(0, 100000);

        // Cas limite : signaler un retard au moment de la réservation
        System.out.print("  Retard de restitution constaté ? (o/n) : ");
        boolean retard = scanner.nextLine().trim().equalsIgnoreCase("o");

        Equipment equip = new Equipment(id, nom, taux, penalite,
                                        new EquipmentPricing());
        if (retard) equip.signalerRetard();

        System.out.println(SEPARATEUR_LEGER);
        System.out.printf("  Équipement : %s (%s)%n", nom, id);
        System.out.printf("  Taux/jour  : %.2f €%n", taux);
        System.out.printf("  Pénalité   : %.2f €%n", penalite);
        System.out.printf("  Retard     : %s%n", retard ? "Oui (pénalité appliquée)" : "Non");
        System.out.println(SEPARATEUR_LEGER);

        return equip;
    }

    /**
     * Guide la création d'un terrain de sport.
     *
     * Paramètres demandés :
     *   - ID et nom
     *   - Prix de base par jour
     *   - Taux de réduction si mauvais temps
     *   - Météo prévue (influence directement le prix)
     *
     * Formule rappelée :
     *   tauxJournalier = baseSport * (1 - reductionPluie) si mauvais temps
     *   prix = tauxJournalier * jours
     */
    private Reservable creerTerrain() {
        System.out.println("\n" + SEPARATEUR_LEGER);
        System.out.println("  TERRAIN DE SPORT");
        System.out.println(SEPARATEUR_LEGER);

        String id  = lireChaine("  ID du terrain (ex: SPORT-01) : ");
        String nom = lireChaine("  Nom du terrain : ");

        System.out.print("  Prix de base par jour (€) : ");
        double base = lireDouble(1, 10000);

        System.out.print("  Réduction si mauvais temps (ex: 0.20 pour 20%) : ");
        double reduction = lireDouble(0, 1);

        System.out.print("  Mauvais temps prévu ? (pluie/neige) (o/n) : ");
        boolean pluie = scanner.nextLine().trim().equalsIgnoreCase("o");

        SportPricing sp      = new SportPricing(base, reduction);
        SportCourt   terrain = new SportCourt(id, nom, sp);
        terrain.setMauvaisTemps(pluie);

        double prixEffectif = pluie ? base * (1 - reduction) : base;
        System.out.println(SEPARATEUR_LEGER);
        System.out.printf("  Terrain       : %s (%s)%n", nom, id);
        System.out.printf("  Base/jour     : %.2f €%n", base);
        System.out.printf("  Réduction     : %.0f%%%n", reduction * 100);
        System.out.printf("  Prix effectif : %.2f €/jour%s%n",
            prixEffectif, pluie ? " (réduction appliquée)" : "");
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
     *   - Liste vide → retour immédiat
     *   - ID inexistant → message clair
     *   - Confirmation refusée → abandon propre
     */
    private void annulerReservation() {
        System.out.println("\n" + SEPARATEUR);
        System.out.println("  ANNULER UNE RÉSERVATION");
        System.out.println(SEPARATEUR);

        if (manager.listerReservations().isEmpty()) {
            System.out.println("  Aucune réservation active.");
            return;
        }

        afficherListeResumee();

        String id = lireChaine("  ID de la réservation à annuler : ");

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
        // et DisponibiliteManager libère automatiquement les créneaux
    }

    // =========================================================
    //  FONCTIONNALITÉ 3 — Lister toutes les réservations
    // =========================================================

    /**
     * Affiche toutes les réservations actives avec leur détail complet.
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
     * Cas limites : liste vide, ID introuvable → messages clairs.
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
     * Utilisé pour tous les paramètres tarifaires.
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