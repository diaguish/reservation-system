package com.reservation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère les créneaux horaires de disponibilité d'une ressource.
 *
 * Stratégie retenue :
 *   Map<LocalDate, boolean[]> — pour chaque jour, un tableau
 *   de 24 booléens. Index = heure (0 à 23).
 *   true  = créneau libre
 *   false = créneau occupé
 *
 * Cas limites gérés automatiquement :
 *   - Réservation multi-jours : le curseur LocalDateTime avance
 *     heure par heure, Java change de jour tout seul via plusHours(1)
 *   - Débordement minuit (ex: 23h + 2h) : même mécanisme,
 *     crée une entrée pour le jour suivant dans la Map
 *   - Jour non initialisé : créé à la demande via computeIfAbsent
 *     avec tous les créneaux à true (libres)
 *
 * SOLID — SRP :
 *   Responsabilité unique : savoir si des créneaux sont libres
 *   et les bloquer/libérer. Aucune logique métier ici.
 */
public class DisponibiliteManager {

    /** Pour chaque date : tableau de 24 créneaux (true = libre) */
    private final Map<LocalDate, boolean[]> planning;

    public DisponibiliteManager() {
        this.planning = new HashMap<>();
    }

    /**
     * Vérifie si une plage horaire consécutive est entièrement disponible.
     *
     * @param debut date et heure de début souhaitées
     * @param duree nombre d'heures consécutives demandées
     * @return true si tous les créneaux de la plage sont libres
     */
    public boolean estDisponible(LocalDateTime debut, long duree) {
        if (duree <= 0) return false;

        LocalDateTime curseur = debut;
        for (long i = 0; i < duree; i++) {
            LocalDate jour    = curseur.toLocalDate();
            int       heure   = curseur.getHour();

            // Si le jour n'est pas dans la Map, tous ses créneaux
            // sont libres par défaut — pas besoin de vérifier
            boolean[] creneaux = planning.get(jour);
            if (creneaux != null && !creneaux[heure]) {
                return false; // créneau occupé
            }

            curseur = curseur.plusHours(1); // gère minuit automatiquement
        }
        return true;
    }

    /**
     * Bloque une plage horaire (marque les créneaux à false).
     * À appeler uniquement après estDisponible() == true.
     */
    public void bloquerCreneaux(LocalDateTime debut, long duree) {
        setCreneaux(debut, duree, false);
    }

    /**
     * Libère une plage horaire (remet les créneaux à true).
     * À appeler lors de l'annulation d'une réservation.
     */
    public void libererCreneaux(LocalDateTime debut, long duree) {
        setCreneaux(debut, duree, true);
    }

    /**
     * Méthode interne commune à bloquer et libérer.
     * Parcourt chaque heure de la plage et affecte la valeur.
     * Crée l'entrée du jour dans la Map si elle n'existe pas encore.
     */
    private void setCreneaux(LocalDateTime debut, long duree, boolean valeur) {
        LocalDateTime curseur = debut;
        for (long i = 0; i < duree; i++) {
            LocalDate jour  = curseur.toLocalDate();
            int       heure = curseur.getHour();

            // Crée le tableau du jour avec tous les créneaux libres
            // si c'est la première opération sur ce jour
            planning.computeIfAbsent(jour, d -> {
                boolean[] c = new boolean[24];
                Arrays.fill(c, true);
                return c;
            });

            planning.get(jour)[heure] = valeur;
            curseur = curseur.plusHours(1);
        }
    }

    /**
     * Retourne une copie des créneaux d'un jour donné.
     * Copie défensive : l'appelant ne peut pas modifier l'état interne.
     * Si le jour est inconnu, retourne un tableau tout à true.
     */
    public boolean[] getCreneauxDuJour(LocalDate jour) {
        boolean[] creneaux = planning.get(jour);
        if (creneaux == null) {
            boolean[] toutLibre = new boolean[24];
            Arrays.fill(toutLibre, true);
            return toutLibre;
        }
        return Arrays.copyOf(creneaux, 24);
    }

    /**
     * Affichage console des créneaux d'un jour.
     * [L] = libre, [X] = occupé
     * Utilisé dans ConsoleMenu pour montrer les disponibilités.
     */
    public void afficherJour(LocalDate jour) {
        boolean[] creneaux = getCreneauxDuJour(jour);
        System.out.print("  " + jour + " : ");
        for (int h = 0; h < 24; h++) {
            if (h == 12) System.out.print("\n             ");
            System.out.printf("[%s]", creneaux[h] ? "L" : "X");
        }
        System.out.println();
        System.out.println("  [L]=libre  [X]=occupé");
    }
}