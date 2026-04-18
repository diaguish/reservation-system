# Système de Réservation de Salles

Projet Java réalisé dans le cadre du cours **INF3133 — Outils pour le développement logiciel**  
ESIEA 3A — Semestre 6 — 2025-2026

---

## Présentation

Application console de gestion de réservations de salles et équipements  
pour plusieurs entreprises. Le système permet de réserver quatre types  
de ressources, de gérer les disponibilités par créneaux horaires,  
et d'annuler des réservations avec libération automatique des créneaux.

---

## Fonctionnalités

- Créer une réservation (salle de réunion, conférence, équipement, terrain de sport)
- Annuler une réservation avec libération automatique des créneaux
- Lister toutes les réservations actives
- Rechercher une réservation par ID
- Afficher le détail complet d'une réservation
- Filtrer les réservations par entreprise
- Vérification de disponibilité par créneaux horaires (gestion multi-jours et débordement minuit)

---

## Types de ressources

| Type | Unité | Formule de prix |
|---|---|---|
| Salle de réunion | Heure | `(baseReunion + capacite × facteur) × duree` |
| Salle de conférence | Heure | `baseConference + max(0, duree - dureeFixe) × extraParHeure` |
| Équipement | Jour | `tauxJournalier × jours (+ penalite si retard)` |
| Terrain de sport | Jour | `baseSport × (1 - reductionPluie) × jours` |

---

## Architecture

src/
├── main/java/com/reservation/
│   ├── Main.java
│   ├── model/
│   │   ├── Reservable.java            ← interface
│   │   ├── PricingStrategy.java       ← interface (Strategy)
│   │   └── DisponibiliteManager.java  ← gestion créneaux horaires
│   ├── model/impl/
│   │   ├── MeetingRoom.java
│   │   ├── ConferenceRoom.java
│   │   ├── Equipment.java
│   │   ├── SportCourt.java
│   │   ├── MeetingPricing.java
│   │   ├── ConferencePricing.java
│   │   ├── EquipmentPricing.java
│   │   └── SportPricing.java
│   ├── manager/
│   │   ├── Reservation.java
│   │   └── ReservationManager.java    ← Singleton + Observable
│   ├── factory/
│   │   └── ReservationFactory.java    ← Factory Method
│   ├── observer/
│   │   ├── ReservationObserver.java   ← interface Observer
│   │   └── ConsoleDisplay.java
│   └── ui/
│       └── ConsoleMenu.java
└── test/java/com/reservation/
└── model/impl/
├── MeetingPricingTest.java
├── ConferencePricingTest.java
├── EquipmentPricingTest.java
└── DisponibiliteManagerTest.java

---

## Design Patterns appliqués

### Strategy
Chaque formule de calcul de prix est encapsulée dans une classe distincte  
qui implémente `PricingStrategy`. Ajouter un nouveau type de tarification  
ne nécessite aucune modification des classes existantes.

### Singleton
`ReservationManager` est instancié une seule fois via `getInstance()`.  
Toutes les réservations transitent par ce point d'accès unique,  
garantissant la cohérence des données.

### Factory Method
`ReservationFactory` centralise la création et la validation des réservations.  
Le `ConsoleMenu` délègue entièrement la construction à la Factory.

### Observer
`ReservationManager` notifie automatiquement ses observateurs à chaque  
création ou annulation. `ConsoleDisplay` implémente `ReservationObserver`  
et affiche les confirmations sans que le menu le demande.

---

## Principes SOLID

| Principe | Application |
|---|---|
| SRP | Chaque classe a une responsabilité unique : `Reservation` porte les données, `ConsoleDisplay` affiche, `ReservationManager` orchestre |
| OCP | Nouveau type de ressource ou de tarification = nouvelle classe, zéro modification de l'existant |
| LSP | Tout `Reservable` est substituable : `ReservationManager` accepte `MeetingRoom`, `SportCourt` etc. sans vérification de type |
| ISP | `Reservable` n'expose que les méthodes communes. Les attributs spécifiques (capacité, pénalité...) restent dans les classes concrètes |
| DIP | `ReservationManager` dépend de `Reservable` et `ReservationObserver` (abstractions), jamais des classes concrètes |

---

## Gestion du temps et des disponibilités

Chaque ressource porte son propre `DisponibiliteManager` contenant  
une `Map<LocalDate, boolean[]>` : pour chaque jour, un tableau de 24 booléens  
représentant les créneaux horaires (index = heure, `true` = libre).

- **Réservation** : bloque les créneaux via `bloquerCreneaux()`
- **Annulation** : libère les créneaux via `libererCreneaux()`
- **Multi-jours** : le curseur `LocalDateTime` avance heure par heure avec `plusHours(1)`, Java gère le changement de jour automatiquement
- **Débordement minuit** : une réservation à 23h pour 2h crée des entrées sur deux jours distincts dans la Map

---

## Prérequis

- Java 17+
- Maven 3.6+

---

## Lancement

```bash
# Compiler et lancer
mvn compile exec:java -Dexec.mainClass="com.reservation.Main"

# Lancer les tests unitaires
mvn test
```

---

## Tests

23 tests unitaires couvrant :

- Calculs de prix pour chaque stratégie (cas nominal, cas limites, erreurs)
- Gestion des disponibilités (`DisponibiliteManager`) : créneaux libres, bloquer, libérer, débordement minuit
- Cas limites : durée nulle, durée négative, réduction invalide, base négative

```bash
mvn test
```

Résultat attendu : `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0`

---

## Workflow GitLab

| Branche | Contenu |
|---|---|
| `main` | Code stable et intégré |
| `feature/core-model` | Modèles et stratégies de prix (Binôme A) |
| `feature/manager-ui` | Manager, Factory, Observer, UI (Binôme B) |
| `feature/sport-court-integration` | Ajout du terrain de sport |
| `feature/launch-config` | Configuration Maven exec plugin |
| `test-unitaires` | Tests unitaires JUnit 5 |

---

## Auteurs
| [Mohamed Naby Ndaw] |Manager, Factory, Observer, UI, SportCourt |
| [Diago Alioune Tall] | Binôme A — Modèles, Stratégies de prix, Tests |

---

## Structure du projet Maven

```xml
<groupId>com.reservation</groupId>
<artifactId>reservation-system</artifactId>
<version>1.0-SNAPSHOT</version>
```