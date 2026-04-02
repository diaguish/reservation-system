package com.reservation.model.impl;

import com.reservation.model.PricingStrategy;
import com.reservation.model.Reservable;

public class MeetingPricing implements PricingStrategy {
     @Override
    public double calculerPrix(Reservable reservable, long duree) {
        if (duree <= 0) {
            throw new IllegalArgumentException("La durée doit être positive");
        }
        if (reservable instanceof MeetingRoom) {
            MeetingRoom room = (MeetingRoom) reservable;
            double prixHoraire= room .getBaseReunion() + room.getFacteur() * room.getCapacite();
            return prixHoraire * duree;
        }
        throw new IllegalArgumentException("La stratégie de prix ne supporte que les salles de réunion");
    }
    
}
