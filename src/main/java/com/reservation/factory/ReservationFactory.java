package com.reservation.factory;

import com.reservation.model.Reservable;

/**
 * Factory pour créer des réservations via le ReservationManager.
 *
 * Design Pattern — Factory Method (cours 3) :
 *   Centralise et encapsule la logique de création.
 *   Le ConsoleMenu n'a pas besoin de connaître comment
 *   une réservation est construite : il délègue à la Factory.
 *
 * Dans notre cas, la Factory joue le rôle de "SimplePizzaFactory"
 * du cours : elle orchestre la création en fonction du type
 * de ressource passé en paramètre.
 *
 * SOLID — SRP :
 *   La seule responsabilité de cette classe est de centraliser
 *   la logique de création / validation avant de déléguer
 *   au ReservationManager.
 *
 * SOLID — OCP :
 *   Pour ajouter un nouveau type de réservation (ex: parking),
 *   on étend la factory sans modifier le Manager ou le Menu.
 */
public class ReservationFactory {

    // Singleton du manager : la Factory le récupère via getInstance()
    private final com.reservation.manager.ReservationManager manager;

    public ReservationFactory() {
        this.manager = com.reservation.manager.ReservationManager.getInstance();
    }

    /**
     * Crée une réservation après validation des paramètres.
     *
     * Cas limites gérés ici (conseil du sujet) :
     *   - duree <= 0 : levée d'exception explicite
     *   - entreprise vide : levée d'exception explicite
     *   - reservable null : levée d'exception explicite
     *
     * @param reservable la ressource à réserver
     * @param debut      date/heure de début
     * @param duree      durée en heures ou en jours
     * @param entreprise nom de l'entreprise cliente
     * @return la réservation créée
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public com.reservation.manager.Reservation creer(
            Reservable reservable,
            java.time.LocalDateTime debut,
            long duree,
            String entreprise) {

        // Validation des entrées (programmation défensive — cours 1)
        if (reservable == null)
            throw new IllegalArgumentException("La ressource ne peut pas être null");
        if (debut == null)
            throw new IllegalArgumentException("La date de début est obligatoire");
        if (duree <= 0)
            throw new IllegalArgumentException("La durée doit être strictement positive");
        if (entreprise == null || entreprise.isBlank())
            throw new IllegalArgumentException("Le nom de l'entreprise est obligatoire");
        if (debut.isBefore(java.time.LocalDateTime.now()))
            throw new IllegalArgumentException("La date de début ne peut pas être dans le passé");

        // Délégation au Manager (qui notifiera les observateurs)
        return manager.creerReservation(reservable, debut, duree, entreprise);
    }
}