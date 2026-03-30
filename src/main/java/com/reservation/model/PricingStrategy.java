package com.reservation.model;

/**
 * Interface du pattern Strategy pour le calcul des prix.
 *
 * Design Pattern — Strategy (cours 3) :
 *   Chaque formule de calcul (réunion, conférence, équipement)
 *   est encapsulée dans une classe séparée qui implémente cette interface.
 *   On peut changer la stratégie de prix sans toucher aux classes
 *   MeetingRoom, ConferenceRoom ou Equipment.
 *
 * SOLID — OCP (Ouvert/Fermé) :
 *   Pour ajouter un nouveau mode de tarification, on crée une nouvelle
 *   classe qui implémente PricingStrategy. Aucun code existant n'est modifié.
 *
 * SOLID — DIP :
 *   Les classes concrètes (MeetingRoom...) dépendent de cette abstraction,
 *   pas d'une implémentation spécifique de calcul.
 */
public interface PricingStrategy {

    /**
     * Calcule le prix d'une réservation.
     *
     * @param reservable la ressource concernée (pour accéder à ses attributs)
     * @param duree      la durée (heures ou jours selon le type)
     * @return le prix total en euros
     */
    double calculerPrix(Reservable reservable, long duree);
}