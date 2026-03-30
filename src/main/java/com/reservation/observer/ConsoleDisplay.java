package com.reservation.observer;

import com.reservation.manager.Reservation;

/**
 * Observateur concret : affiche les événements dans la console.
 *
 * Design Pattern — Observer (cours 3) :
 *   Implémente ReservationObserver.
 *   Est enregistré auprès de ReservationManager au démarrage.
 *   Chaque fois qu'une réservation est créée ou annulée,
 *   le Manager appelle update() sur tous ses observateurs,
 *   dont celui-ci.
 *
 * SOLID — SRP :
 *   Cette classe a une seule responsabilité : l'affichage console.
 *   Elle ne contient aucune logique métier.
 */
public class ConsoleDisplay implements ReservationObserver {

    /**
     * Affichage lors d'une création de réservation.
     */
    @Override
    public void onReservationCreee(Reservation reservation) {
        System.out.println("\n[NOUVEAU] Réservation créée :");
        System.out.println(reservation.toString());
        System.out.println("----------------------------------------");
    }

    /**
     * Affichage lors d'une annulation de réservation.
     */
    @Override
    public void onReservationAnnulee(Reservation reservation) {
        System.out.println("\n[ANNULÉ] Réservation supprimée :");
        System.out.println("  ID : " + reservation.getId());
        System.out.println("  Ressource : " + reservation.getReservable().getNom());
        System.out.println("----------------------------------------");
    }
}