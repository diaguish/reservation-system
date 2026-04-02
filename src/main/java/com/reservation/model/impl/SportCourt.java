package com.reservation.model.impl;

import com.reservation.model.DisponibiliteManager;
import com.reservation.model.PricingStrategy;
import com.reservation.model.Reservable;

import java.time.LocalDateTime;

/**
 * Terrain de sport réservable à la journée.
 *
 * SOLID — LSP (Substitution de Liskov) :
 *   SportCourt implémente Reservable exactement comme
 *   MeetingRoom ou Equipment. ReservationManager peut
 *   l'utiliser sans aucune modification ni vérification de type.
 *
 * SOLID — SRP :
 *   Cette classe porte uniquement les données du terrain.
 *   Le calcul du prix est délégué à SportPricing (Strategy).
 *   La disponibilité est déléguée à DisponibiliteManager.
 *
 * Pas de capacité de personnes — le sujet ne l'exige pas
 * pour ce type. On respecte ISP : on n'implémente que ce
 * qui a du sens pour cette ressource.
 */
public class SportCourt implements Reservable {

    private final String id;
    private final String nom;

    /**
     * La stratégie est typée SportPricing (et pas juste PricingStrategy)
     * pour pouvoir appeler setMauvaisTemps() avant le calcul.
     *
     * SOLID — DIP respecté malgré tout : Reservable dépend de
     * PricingStrategy (abstraction). SportCourt connaît le type
     * concret uniquement pour accéder à la fonctionnalité météo
     * qui lui est propre.
     */
    private final SportPricing pricingStrategy;

    /** Gestionnaire de créneaux propre à ce terrain */
    private final DisponibiliteManager disponibilite;

    public SportCourt(String id, String nom, SportPricing pricingStrategy) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("L'ID ne peut pas être vide");
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (pricingStrategy == null)
            throw new IllegalArgumentException("La stratégie de prix est obligatoire");

        this.id              = id;
        this.nom             = nom;
        this.pricingStrategy = pricingStrategy;
        this.disponibilite   = new DisponibiliteManager();
    }

    // --- Reservable ---

    @Override
    public String getId() { return id; }

    @Override
    public String getNom() { return nom; }

    @Override
    public String getDescription() {
        return String.format(
            "Terrain de sport | %.2f €/jour | Réduction pluie : %.0f%%",
            pricingStrategy.getBaseSport(),  // getter à ajouter dans SportPricing
            pricingStrategy.getReductionPluie() * 100
        );
    }

    /**
     * Calcule le prix.
     * Si le temps est mauvais, la réduction est déjà configurée
     * dans SportPricing via setMauvaisTemps() avant cet appel.
     */
    @Override
    public double calculerPrix(long duree) {
        return pricingStrategy.calculerPrix(this, duree);
    }

    @Override
    public boolean estDisponible(LocalDateTime debut, long duree) {
        return disponibilite.estDisponible(debut, duree);
    }

    @Override
    public DisponibiliteManager getDisponibiliteManager() {
        return disponibilite;
    }

    /**
     * Permet de signaler les conditions météo avant la réservation.
     * Appelé par le ConsoleMenu quand l'utilisateur indique la météo.
     */
    public void setMauvaisTemps(boolean mauvaisTemps) {
        pricingStrategy.setMauvaisTemps(mauvaisTemps);
    }

    public boolean isMauvaisTemps() {
        return pricingStrategy.isMauvaisTemps();
    }
}