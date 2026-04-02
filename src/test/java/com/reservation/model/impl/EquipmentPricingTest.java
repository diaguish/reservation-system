package com.reservation.model.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentPricingTest {

    private EquipmentPricing strategy;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        strategy = new EquipmentPricing();
        // 50€/jour, pénalité 30€
        equipment = new Equipment("E1", "Projecteur", 50.0, 30.0, strategy);
    }

    @Test
    void testPrixSansRetard() {
        // 50 * 3 = 150€
        assertEquals(150.0, equipment.calculerPrix(3));
    }

    @Test
    void testPrixAvecRetard() {
        // 50 * 3 + 30 = 180€
        equipment.signalerRetard();
        assertEquals(180.0, equipment.calculerPrix(3));
    }

    @Test
    void testPrixUnJourSansRetard() {
        // 50 * 1 = 50€
        assertEquals(50.0, equipment.calculerPrix(1));
    }

    @Test
    void testReinitialiserRetard() {
        // on signale puis on réinitialise → pas de pénalité
        equipment.signalerRetard();
        equipment.reinitialiserRetard();
        assertEquals(150.0, equipment.calculerPrix(3));
    }

    @Test
    void testDureeNegativeLeveException() {
        assertThrows(IllegalArgumentException.class, () -> equipment.calculerPrix(-1));
    }

    @Test
    void testDureeZeroLeveException() {
        assertThrows(IllegalArgumentException.class, () -> equipment.calculerPrix(0));
    }
}