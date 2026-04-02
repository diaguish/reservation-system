package com.reservation.model;

import java.time.LocalDateTime;

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

    String getId();
    String getNom();
    String getDescription();
    double calculerPrix(long duree);

    /**
     * Vérifie si la ressource est disponible sur une plage donnée.
     * Délègue à son DisponibiliteManager.
     *
     * @param debut date et heure de début souhaitées
     * @param duree nombre d'heures demandées
     * @return true si tous les créneaux sont libres
     */
    boolean estDisponible(LocalDateTime debut, long duree);

    /**
     * Expose le gestionnaire de disponibilité de cette ressource.
     * Utilisé par ReservationManager pour bloquer/libérer les créneaux.
     *
     * SOLID — DIP :
     *   ReservationManager n'accède jamais au DisponibiliteManager
     *   directement depuis une classe concrète. Il passe par cette
     *   méthode définie dans l'abstraction.
     */
    DisponibiliteManager getDisponibiliteManager();

}