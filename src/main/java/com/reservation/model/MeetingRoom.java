package com.reservation.model;

import java.time.LocalDateTime;

public class MeetingRoom implements Reservable {

    // --- Attributs ---
    private final String id;
    private final String nom;
    private final int capacite;
    private final double baseReunion;
    private final double facteur;
    private final PricingStrategy strategy;
    
    // Chaque salle a son propre planning, jamais partagé
    private final DisponibiliteManager disponibilite = new DisponibiliteManager();

    // --- Constructeur ---
    public MeetingRoom(String id, String nom, int capacite, 
                       double baseReunion, double facteur, 
                       PricingStrategy strategy) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("L'id ne peut pas être vide");
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (capacite <= 0)
            throw new IllegalArgumentException("La capacité doit être positive");
        if (baseReunion < 0)
            throw new IllegalArgumentException("La base ne peut pas être négative");
        if (facteur < 0)
            throw new IllegalArgumentException("Le facteur ne peut pas être négatif");
        if (strategy == null)
            throw new IllegalArgumentException("La stratégie ne peut pas être null");

        this.id = id;
        this.nom = nom;
        this.capacite = capacite;
        this.baseReunion = baseReunion;
        this.facteur = facteur;
        this.strategy = strategy;
    }

    // --- Reservable ---
    @Override
    public String getId() { return id; }

    @Override
    public String getNom() { return nom; }

    @Override
    public String getDescription() {
        return String.format("%s (Capacité: %d personnes)", nom, capacite);
    }

    @Override
    public double calculerPrix(long duree) {
        // Délègue le calcul à la stratégie — pattern Strategy
        return strategy.calculerPrix(this, duree);
    }

    @Override
    public boolean estDisponible(LocalDateTime debut, long duree) {
        // Délègue au planning de la salle
        return disponibilite.estDisponible(debut, duree);
    }

    @Override
    public DisponibiliteManager getDisponibiliteManager() {
        // Exposé pour que ReservationManager puisse bloquer/libérer
        return disponibilite;
    }

    // --- Getters spécifiques (utilisés par MeetingPricing) ---
    public int getCapacite()       { return capacite; }
    public double getBaseReunion() { return baseReunion; }
    public double getFacteur()     { return facteur; }
}