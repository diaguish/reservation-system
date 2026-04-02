package com.reservation.model;

import java.time.LocalDateTime;

public class Equipment implements Reservable {

    // --- Attributs ---
    private final String id;
    private final String nom;
    private final double tauxJournalier;  
    private final double penalite;        
    private boolean retard;      
    private final PricingStrategy strategy; 
    private final DisponibiliteManager disponibilite = new DisponibiliteManager();

    // --- Constructeur ---
    public Equipment(String id, String nom, double tauxJournalier,
                     double penalite, PricingStrategy strategy) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("L'id ne peut pas être vide");
        if (nom == null || nom.isBlank())
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        if (tauxJournalier < 0)
            throw new IllegalArgumentException("Le taux ne peut pas être négatif");
        if (penalite < 0)
            throw new IllegalArgumentException("La pénalité ne peut pas être négative");
        if (strategy == null)
            throw new IllegalArgumentException("La stratégie ne peut pas être null");

        this.id = id;
        this.nom = nom;
        this.tauxJournalier = tauxJournalier;
        this.penalite = penalite;
        this.retard = false; 
        this.strategy = strategy;
    }

    // --- Reservable ---
    @Override
    public String getId() { return id; }

    @Override
    public String getNom() { return nom; }

    @Override
    public String getDescription() {
        return String.format("%s (%.2f€/jour)", nom, tauxJournalier);
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

    // --- Getters spécifiques (utilisés par EquipmentPricing) ---
    public double getTauxJournalier() {
         return tauxJournalier;
         }
    public double getPenalite()       { 
        return penalite;
     }
    public boolean isRetard()         { 
        return retard; 
    }

    // --- Setter retard uniquement ---
    public void signalerRetard()      {
         this.retard = true;
         }
    public void reinitialiserRetard() { 
        this.retard = false;
        }
}