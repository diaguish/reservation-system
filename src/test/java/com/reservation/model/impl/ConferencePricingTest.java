package com.reservation.model.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConferencePricingTest {

    private ConferencePricing strategy;
    private ConferenceRoom room;

    @BeforeEach
    void setUp() {
        strategy = new ConferencePricing();
        // forfait 200€, 4h incluses, 30€/h de dépassement
        room = new ConferenceRoom("C1", "Salle Conf", 200.0, 4, 30.0, strategy);
    }

    @Test
    void testPrixSansDepassement() {
        // duree <= dureeFixe → on paye juste le forfait
        assertEquals(200.0, room.calculerPrix(3));
    }

    @Test
    void testPrixExactementDureeForfait() {
        // duree == dureeFixe → pas de dépassement
        assertEquals(200.0, room.calculerPrix(4));
    }

    @Test
    void testPrixAvecDepassement() {
        // 200 + (6-4) * 30 = 200 + 60 = 260€
        assertEquals(260.0, room.calculerPrix(6));
    }

    @Test
    void testDureeNegativeLeveException() {
        assertThrows(IllegalArgumentException.class, () -> room.calculerPrix(-1));
    }

    @Test
    void testDureeZeroLeveException() {
        assertThrows(IllegalArgumentException.class, () -> room.calculerPrix(0));
    }
}