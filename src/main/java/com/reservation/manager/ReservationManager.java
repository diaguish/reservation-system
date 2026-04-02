package com.reservation.manager;

import com.reservation.model.Reservable;
import com.reservation.observer.ReservationObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestionnaire central des réservations.
 *
 * Design Pattern — Singleton (cours 3) :
 *   Une seule instance existe dans toute l'application.
 *   Utilisé car c'est une ressource partagée (la liste des réservations)
 *   accessible depuis plusieurs parties du programme (UI, tests...).
 *   Le constructeur est privé, l'accès se fait via getInstance().
 *
 * Design Pattern — Observer (cours 3) :
 *   Le Manager est le "Sujet" (Observable).
 *   Il maintient une liste d'observateurs (ReservationObserver).
 *   À chaque création ou annulation, il notifie tous ses observateurs.
 *   Il ne connaît pas ConsoleDisplay directement : couplage faible.
 *
 * SOLID — SRP :
 *   Responsabilité unique : gérer le cycle de vie des réservations.
 *   L'affichage est délégué aux observateurs.
 *   La création des objets est déléguée à ReservationFactory.
 *
 * SOLID — DIP :
 *   Dépend de Reservable et ReservationObserver (abstractions),
 *   jamais des classes concrètes.
 */
public class ReservationManager {

    // --- Singleton ---

    /** Instance unique, volatile pour la thread-safety */
    private static volatile ReservationManager instance = null;

    /** Constructeur privé : personne ne peut faire new ReservationManager() */
    private ReservationManager() {
        this.reservations = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Point d'accès global à l'instance unique.
     * Double-checked locking pour la thread-safety (cours 3 — synchronized).
     */
    public static ReservationManager getInstance() {
        if (instance == null) {
            synchronized (ReservationManager.class) {
                if (instance == null) {
                    instance = new ReservationManager();
                }
            }
        }
        return instance;
    }

    // --- Données ---

    /** Liste de toutes les réservations actives */
    private final List<Reservation> reservations;

    /** Compteur pour générer des IDs uniques */
    private int compteurId = 1;

    // --- Observer ---

    /** Liste des observateurs enregistrés */
    private final List<ReservationObserver> observers;

    /**
     * Enregistre un observateur.
     * Pattern Observer — équivalent de registerObserver() du cours.
     */
    public void ajouterObserver(ReservationObserver observer) {
        observers.add(observer);
    }

    /**
     * Retire un observateur.
     * Pattern Observer — équivalent de removeObserver() du cours.
     */
    public void retirerObserver(ReservationObserver observer) {
        observers.remove(observer);
    }

    /** Notifie tous les observateurs d'une création */
    private void notifierCreation(Reservation reservation) {
        for (ReservationObserver observer : observers) {
            observer.onReservationCreee(reservation);
        }
    }

    /** Notifie tous les observateurs d'une annulation */
    private void notifierAnnulation(Reservation reservation) {
        for (ReservationObserver observer : observers) {
            observer.onReservationAnnulee(reservation);
        }
    }

    // --- Logique métier ---

    /**
     * Crée et enregistre une nouvelle réservation.
     *
     * @param reservable la ressource à réserver
     * @param debut      date et heure de début
     * @param duree      durée (heures ou jours)
     * @param entreprise nom de l'entreprise
     * @return la réservation créée
     */
    public Reservation creerReservation(Reservable reservable,
                                    LocalDateTime debut,
                                    long duree,
                                    String entreprise) {

    // NOUVEAU — vérification disponibilité avant toute création
    if (!reservable.estDisponible(debut, duree)) {
        throw new IllegalStateException(
            "La ressource '" + reservable.getNom() +
            "' n'est pas disponible sur ce créneau " +
            "(début: " + debut + ", durée: " + duree + "h)."
        );
    }

    String id = String.format("RES-%03d", compteurId++);
    Reservation reservation = new Reservation(id, reservable, debut, duree, entreprise);
    reservations.add(reservation);

    // NOUVEAU — bloquer les créneaux après création confirmée
    reservable.getDisponibiliteManager().bloquerCreneaux(debut, duree);

    notifierCreation(reservation);
    return reservation;
}

    /**
     * Annule une réservation existante par son ID.
     *
     * @param id l'identifiant de la réservation à annuler
     * @return true si trouvée et annulée, false sinon
     */
    public boolean annulerReservation(String id) {
    if (id == null || id.isBlank()) return false;

    for (Reservation r : reservations) {
        if (r.getId().equals(id)) {
            reservations.remove(r);

            // NOUVEAU — libérer les créneaux à l'annulation
            r.getReservable()
            .getDisponibiliteManager()
            .libererCreneaux(r.getDebut(), r.getDuree());

            notifierAnnulation(r);
            return true;
        }
    }
    return false;
}

    /**
     * Retourne une vue non-modifiable de toutes les réservations.
     * On retourne une copie non-modifiable pour protéger l'état interne
     * (encapsulation — cours 1).
     */
    public List<Reservation> listerReservations() {
        return Collections.unmodifiableList(reservations);
    }

    /**
     * Recherche une réservation par son ID.
     * @return la réservation ou null si non trouvée
     */
    public Reservation trouverParId(String id) {
        return reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Réinitialise le manager (utile pour les tests unitaires).
     * Permet de repartir d'un état propre entre deux tests.
     */
    public void reset() {
        reservations.clear();
        compteurId = 1;
    }
}