package com.reservation.model;

public class ConferencePricing implements PricingStrategy {

    @Override
    public double calculerPrix(Reservable reservable, long duree) {
        if (!(reservable instanceof ConferenceRoom)) {
            throw new IllegalArgumentException(
                "ConferencePricing attend un ConferenceRoom");
        }
        
        ConferenceRoom room = (ConferenceRoom) reservable;
        

        double prix = room.getBaseConference();
        
       
        if (duree > room.getDureeFixe()) {
            prix += (duree - room.getDureeFixe()) * room.getExtraParHeure();
        }
        
        return prix;
    }
}
