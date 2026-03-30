package com.reservation.observer;

import com.reservation.manager.Reservation;

/**
 * Interface Observer du pattern Observateur (cours 3).
 *
 * Design Pattern — Observer :
 *   Tout composant qui veut être notifié des changements
 *   dans ReservationManager doit implémenter cette interface.
 *   Le Manager (Sujet) ne connaît ses observateurs qu'à travers
 *   cette interface : couplage faible garanti.
 *
 * SOLID — DIP :
 *   ReservationManager dépend de ReservationObserver (abstraction),
 *   jamais de ConsoleDisplay directement.
 *
 * SOLID — OCP :
 *   On peut ajouter un FileLogger, un DatabaseObserver, etc.
 *   sans toucher à ReservationManager.
 */
public interface ReservationObserver {

    /**
     * Appelé par le Manager quand une réservation est créée.
     * @param reservation la nouvelle réservation
     */
    void onReservationCreee(Reservation reservation);

    /**
     * Appelé par le Manager quand une réservation est annulée.
     * @param reservation la réservation annulée
     */
    void onReservationAnnulee(Reservation reservation);
}