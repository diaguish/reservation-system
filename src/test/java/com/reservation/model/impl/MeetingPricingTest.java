package com.reservation.model.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MeetingPricingTest {

    private MeetingPricing strategy;
    private MeetingRoom room;

    @BeforeEach
    void setUp() {
        strategy = new MeetingPricing();
        // prixHoraire = 10 + (5 * 2) = 20€/h
        room = new MeetingRoom("R1", "Salle Alpha", 5, 10.0, 2.0, strategy);
    }

    @Test
    void testPrix1Heure() {
        // 20€/h * 1h = 20€
        assertEquals(20.0, room.calculerPrix(1));
    }

    @Test
    void testPrix3Heures() {
        // 20€/h * 3h = 60€
        assertEquals(60.0, room.calculerPrix(3));
    }

    @Test
    void testPrixAvecGrandeCapacite() {
        // prixHoraire = 10 + (10 * 2) = 30€/h * 2h = 60€
        MeetingRoom grandeRoom = new MeetingRoom("R2", "Grande Salle", 10, 10.0, 2.0, strategy);
        assertEquals(60.0, grandeRoom.calculerPrix(2));
    }

    @Test
    void testDureeNegativeLeveException() {
        // une durée négative doit lever une exception
        assertThrows(IllegalArgumentException.class, () -> room.calculerPrix(-1));
    }

    @Test
    void testDureeZeroLeveException() {
        // une durée à 0 doit lever une exception
        assertThrows(IllegalArgumentException.class, () -> room.calculerPrix(0));
    }

    @Test
    void testMauvaisTypeLeveException() {
        // passer un mauvais type doit lever une exception
        MeetingPricing p = new MeetingPricing();
        ConferenceRoom mauvaisType = new ConferenceRoom("C1", "Conf", 200.0, 4, 30.0, new ConferencePricing());
        assertThrows(IllegalArgumentException.class, () -> p.calculerPrix(mauvaisType, 2));
    }
}