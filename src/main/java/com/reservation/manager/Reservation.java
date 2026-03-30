package com.reservation.manager;

import com.reservation.model.Reservable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une réservation dans le système.
 *
 * SOLID — SRP :
 * Cette classe ne fait que porter les données d'une réservation.
 * Elle ne calcule pas, ne s'affiche pas, ne se persiste pas.
 *
 * Note sur la gestion du temps (conseil du sujet) :
 * On utilise LocalDateTime de java.time (API moderne Java 8+)
 * plutôt que Date ou Calendar pour éviter les pièges classiques
 * (fuseaux horaires, mutabilité...).
 */
public class Reservation {

    // Formatter pour l'affichage lisible des dates
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Identifiant unique de la réservation (ex: "RES-001") */
    private final String id;

    /** La ressource réservée (salle ou équipement) */
    private final Reservable reservable;

    /** Date et heure de début */
    private final LocalDateTime debut;

    /** Durée en heures (salles) ou en jours (équipements) */
    private final long duree;

    /** Prix total calculé au moment de la création */
    private final double prixTotal;

    /** Nom de l'entreprise qui effectue la réservation */
    private final String entreprise;

    /**
     * Constructeur complet.
     * Le prix est calculé immédiatement via reservable.calculerPrix().
     */
    public Reservation(String id, Reservable reservable,
            LocalDateTime debut, long duree, String entreprise) {
        // Cas limite : vérifications défensives (cours 1 — fail fast)
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("L'ID de réservation ne peut pas être vide");
        if (reservable == null)
            throw new IllegalArgumentException("La ressource réservable ne peut pas être null");
        if (debut == null)
            throw new IllegalArgumentException("La date de début ne peut pas être null");
        if (duree <= 0)
            throw new IllegalArgumentException("La durée doit être positive");
        if (entreprise == null || entreprise.isBlank())
            throw new IllegalArgumentException("L'entreprise ne peut pas être vide");

        this.id = id;
        this.reservable = reservable;
        this.debut = debut;
        this.duree = duree;
        this.entreprise = entreprise;

        // Le prix est délégué à la ressource (qui délègue à sa PricingStrategy)
        this.prixTotal = reservable.calculerPrix(duree);
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public Reservable getReservable() {
        return reservable;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public long getDuree() {
        return duree;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public String getEntreprise() {
        return entreprise;
    }

    /**
     * Représentation textuelle lisible pour la console.
     */
    @Override
    public String toString() {
        return String.format(
                "  ID         : %s%n" +
                        "  Entreprise : %s%n" +
                        "  Ressource  : %s (%s)%n" +
                        "  Début      : %s%n" +
                        "  Durée      : %d unité(s)%n" +
                        "  Prix total : %.2f €",
                id, entreprise,
                reservable.getNom(), reservable.getId(),
                debut.format(FORMATTER),
                duree, prixTotal);
    }
}