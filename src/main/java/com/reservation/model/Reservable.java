package com.reservation.model;

/**
 * Interface commune à tout objet réservable dans le système.
 *
 * SOLID — ISP (Ségrégation des interfaces) :
 *   On ne met ici que ce qui est strictement commun à tous les types.
 *   Aucune méthode spécifique à un type particulier (ex: capacité, retard...)
 *   ne se trouve ici.
 *
 * SOLID — LSP (Substitution de Liskov) :
 *   Partout où on attend un Reservable, on doit pouvoir passer
 *   une MeetingRoom, une ConferenceRoom ou un Equipment sans
 *   que le comportement du programme change.
 *
 * SOLID — DIP (Inversion des dépendances) :
 *   ReservationFactory et ReservationManager dépendent de
 *   cette abstraction, jamais des classes concrètes.
 */
public interface Reservable {

    /** Identifiant unique de la ressource (ex: "SALLE-01") */
    String getId();

    /** Nom lisible de la ressource (ex: "Salle Einstein") */
    String getNom();

    /**
     * Calcule le prix d'une réservation.
     *
     * @param duree durée en heures pour les salles, en jours pour les équipements
     * @return le prix total en euros
     */
    double calculerPrix(long duree);

    /**
     * Description courte affichée dans le menu console.
     * Chaque type de ressource retourne une description adaptée.
     */
    String getDescription();
}