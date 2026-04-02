package com.reservation.model.impl;

import java.time.LocalDateTime;

import com.reservation.model.DisponibiliteManager;
import com.reservation.model.PricingStrategy;
import com.reservation.model.Reservable;

public class ConferenceRoom implements Reservable {

    // --- Attributs ---
    private final String id;
    private final String nom;
    private final double baseConference; // prix du forfait
    private final long dureeFixe;         // durée incluse dans le forfait
    private final double extraParHeure;   // supplément si dépassement
    private final PricingStrategy strategy;

    private final DisponibiliteManager disponibilite = new DisponibiliteManager();

    // --- Constructeur ---
    public ConferenceRoom(String id, String nom, double baseConference,
                          long dureeFixe, double extraParHeure,
                          PricingStrategy strategy) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("L'id ne peut pas être vide");
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (baseConference < 0)
            throw new IllegalArgumentException("La base ne peut pas être négative");
        if (dureeFixe <= 0)
            throw new IllegalArgumentException("La durée fixe doit être positive");
        if (extraParHeure < 0)
            throw new IllegalArgumentException("L'extra ne peut pas être négatif");
        if (strategy == null)
            throw new IllegalArgumentException("La stratégie ne peut pas être null");

        this.id = id;
        this.nom = nom;
        this.baseConference = baseConference;
        this.dureeFixe = dureeFixe;
        this.extraParHeure = extraParHeure;
        this.strategy = strategy;
    }

    // --- Reservable ---
    @Override
    public String getId() { return id; }

    @Override
    public String getNom() { return nom; }

    @Override
    public String getDescription() {
        return String.format("%s (Forfait: %dh à %.2f€)", nom, dureeFixe, baseConference);
    }

    @Override
    public double calculerPrix(long duree) {
        return strategy.calculerPrix(this, duree);
    }

    @Override
    public boolean estDisponible(LocalDateTime debut, long duree) {
        return disponibilite.estDisponible(debut, duree);
    }

    @Override
    public DisponibiliteManager getDisponibiliteManager() {
        return disponibilite;
    }

    // --- Getters spécifiques (utilisés par ConferencePricing) ---
    public double getBaseConference() { 
        return baseConference;
     }
    public long getDureeFixe()        { 
        return dureeFixe; 
     }
    public double getExtraParHeure()  { 
        return extraParHeure; 
     }
}