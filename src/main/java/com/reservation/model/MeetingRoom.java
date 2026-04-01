package com.reservation.model;

public class MeetingRoom implements Reservable {

    private String id;
    private String nom;
    private int capacite;
    private double baseReunion;
    private double facteur;
    private PricingStrategy strategy;


    public MeetingRoom(String id, String nom, int capacite, double baseReunion, double facteur, PricingStrategy strategy) {
     if (id == null || id.isBlank())
    throw new IllegalArgumentException("L'id ne peut pas être vide");
      this.id = id;
    if (nom == null || nom.isBlank())
        throw new IllegalArgumentException("Le nom ne peut pas être vide"); 
     this.nom = nom;
    if (capacite <= 0)
        throw new IllegalArgumentException("La capacité doit être positive");
    this.capacite = capacite;
    if (baseReunion < 0)        throw new IllegalArgumentException("Le prix de base doit être positif");
    this.baseReunion = baseReunion;
    if (facteur < 0)        throw new IllegalArgumentException("Le facteur doit être positif");    
    this.facteur = facteur;
    if (strategy == null)
        throw new IllegalArgumentException("La stratégie de prix ne peut pas être null");
    this.strategy = strategy;
   
}

public int getCapacite() {
    return capacite;
}

public double getBaseReunion() {
    return baseReunion; 
}

public double getFacteur() {
    return facteur;
}

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public String getDescription() {
        return String.format("%s (Capacité: %d personnes)", nom, capacite);
    }

    @Override
    public double calculerPrix(long duree) {
        return strategy.calculerPrix(this, duree);
    }
}