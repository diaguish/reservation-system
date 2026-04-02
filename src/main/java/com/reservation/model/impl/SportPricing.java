package com.reservation.model.impl;

import com.reservation.model.PricingStrategy;
import com.reservation.model.Reservable;

/**
 * Stratégie de prix pour un terrain de sport.
 *
 * Design Pattern — Strategy (cours 3) :
 * Implémente PricingStrategy comme les autres stratégies.
 * Aucune autre classe n'est modifiée pour accueillir ce nouveau
 * type de tarification — c'est exactement le but du pattern.
 *
 * SOLID — OCP :
 * On étend le comportement du système de pricing sans toucher
 * à MeetingPricing, ConferencePricing ou EquipmentPricing.
 *
 * Formule :
 * prix = tauxJournalier * jours
 * tauxJournalier = baseSport * (1 - reductionPluie)
 * La réduction ne s'applique que si mauvaisTemps == true.
 */
public class SportPricing implements PricingStrategy {

    /** Prix de base par jour sans réduction */
    private final double baseSport;

    /**
     * Taux de réduction en cas de pluie ou neige (ex: 0.20 = 20%).
     * Entre 0.0 et 1.0.
     */
    private final double reductionPluie;

    /**
     * Indique si le temps est mauvais (pluie ou neige).
     * Peut être mis à jour avant le calcul via setMauvaisTemps().
     */
    private boolean mauvaisTemps;

    public double getBaseSport() {
        return baseSport;
    }

    public double getReductionPluie() {
        return reductionPluie;
    }

    public SportPricing(double baseSport, double reductionPluie) {
        if (baseSport <= 0)
            throw new IllegalArgumentException("La base sport doit être positive");
        if (reductionPluie < 0 || reductionPluie > 1)
            throw new IllegalArgumentException("La réduction doit être entre 0 et 1");

        this.baseSport = baseSport;
        this.reductionPluie = reductionPluie;
        this.mauvaisTemps = false; // beau temps par défaut
    }

    /**
     * Met à jour la condition météo avant le calcul du prix.
     * Appelé par le ConsoleMenu ou un service météo externe.
     *
     * @param mauvaisTemps true si pluie ou neige le jour de la réservation
     */
    public void setMauvaisTemps(boolean mauvaisTemps) {
        this.mauvaisTemps = mauvaisTemps;
    }

    public boolean isMauvaisTemps() {
        return mauvaisTemps;
    }

    /**
     * Calcule le prix total de la réservation.
     *
     * Si mauvais temps :
     * tauxJournalier = baseSport * (1 - reductionPluie)
     * Sinon :
     * tauxJournalier = baseSport
     *
     * prix = tauxJournalier * jours
     *
     * @param reservable la ressource (non utilisée ici car pas de capacité)
     * @param duree      nombre de jours de réservation
     */
    @Override
    public double calculerPrix(Reservable reservable, long duree) {
        if (duree <= 0)
            throw new IllegalArgumentException("La durée doit être positive");

        double tauxJournalier = mauvaisTemps
                ? baseSport * (1 - reductionPluie)
                : baseSport;

        return tauxJournalier * duree;
    }
}