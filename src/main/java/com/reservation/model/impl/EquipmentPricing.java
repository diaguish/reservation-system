package com.reservation.model.impl;

import com.reservation.model.PricingStrategy;
import com.reservation.model.Reservable;

public class EquipmentPricing implements PricingStrategy {

    @Override
    public double calculerPrix(Reservable reservable, long duree) {
        if (!(reservable instanceof Equipment)) {
            throw new IllegalArgumentException(
                "EquipmentPricing attend un Equipment");
        }

        Equipment equipment = (Equipment) reservable;

        // Prix de base — taux journalier * nombre de jours
        double prix = equipment.getTauxJournalier() * duree;

        // Pénalité si retard de restitution
        if (equipment.isRetard()) {
            prix += equipment.getPenalite();
        }

        return prix;
    }
}
