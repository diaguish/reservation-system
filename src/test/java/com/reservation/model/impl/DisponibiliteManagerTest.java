package com.reservation.model.impl;

import com.reservation.model.DisponibiliteManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DisponibiliteManagerTest {

    private DisponibiliteManager manager;

    @BeforeEach
    void setUp() {
        manager = new DisponibiliteManager();
    }

    @Test
    void testDisponibleParDefaut() {
        // un créneau jamais réservé est libre par défaut
        LocalDateTime debut = LocalDateTime.of(2026, 4, 10, 9, 0);
        assertTrue(manager.estDisponible(debut, 2));
    }

    @Test
    void testBloquerEtVerifier() {
        // on bloque 9h-11h → plus disponible
        LocalDateTime debut = LocalDateTime.of(2026, 4, 10, 9, 0);
        manager.bloquerCreneaux(debut, 2);
        assertFalse(manager.estDisponible(debut, 2));
    }

    @Test
    void testLibererApresAnnulation() {
        // on bloque puis on libère → disponible à nouveau
        LocalDateTime debut = LocalDateTime.of(2026, 4, 10, 9, 0);
        manager.bloquerCreneaux(debut, 2);
        manager.libererCreneaux(debut, 2);
        assertTrue(manager.estDisponible(debut, 2));
    }

    @Test
    void testDebordementMinuit() {
        // réservation de 23h à 1h du matin → passage minuit
        LocalDateTime debut = LocalDateTime.of(2026, 4, 10, 23, 0);
        manager.bloquerCreneaux(debut, 2);
        assertFalse(manager.estDisponible(debut, 2));
    }

    @Test
    void testCreneauPartielOccupe() {
        // 9h bloqué → une réservation 8h-10h doit échouer
        LocalDateTime debut = LocalDateTime.of(2026, 4, 10, 9, 0);
        manager.bloquerCreneaux(debut, 1);
        LocalDateTime debutElargi = LocalDateTime.of(2026, 4, 10, 8, 0);
        assertFalse(manager.estDisponible(debutElargi, 3));
    }

    @Test
    void testDureeNegativeRetourneFaux() {
        LocalDateTime debut = LocalDateTime.of(2026, 4, 10, 9, 0);
        assertFalse(manager.estDisponible(debut, -1));
    }
}