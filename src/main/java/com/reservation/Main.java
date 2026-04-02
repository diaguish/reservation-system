package com.reservation;

import com.reservation.ui.ConsoleMenu;

/**
 * Point d'entrée de l'application.
 *
 * SOLID — SRP :
 *   Cette classe ne fait qu'une chose : instancier le menu
 *   et le démarrer. Toute la logique est ailleurs.
 */
public class Main {

    public static void main(String[] args) {
        ConsoleMenu menu = new ConsoleMenu();
        menu.demarrer();
    }
}