package GamePlay;

import GamePlay.Cards.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Trieda reprezentuje balíček v ktorom sa nachádzajú všetky karty ktoré sú dostupné v hre.
 * Karty sú uložené v LinkedListe. Počty a atribúty jednotlivých unikátnych kariet sa načítavajú z externých súborov.
 * Táto trieda poskytuje metódy pre načítavanie zo súborov.
 * Ak sa nepodarí načítať danú hodnotu zo súboru tak sa použije preddefinovaná konštanta ktorá sa nachádza v tejto triede.
 */
public class Deck {

    private LinkedList<Card> cards;
    private int unPickedCards;

    private Map<String, Integer> numberOfCards;
    private Map<String, Integer> weaponRanges;
    private Map<String, Integer> weaponDamage;
    private Map<String, Integer> shieldProtections;
    private Map<String, Integer> helmPlusRange;
    private Map<String, Integer> helmMinusRange;

    private URL urlOfFolder;

    public Deck() {
        this.cards = new LinkedList<>();
        this.numberOfCards = new HashMap<>();
        this.weaponRanges = new HashMap<>();
        this.weaponDamage = new HashMap<>();
        this.shieldProtections = new HashMap<>();
        this.helmPlusRange = new HashMap<>();
        this.helmMinusRange = new HashMap<>();
        this.urlOfFolder =  getClass().getProtectionDomain().getCodeSource().getLocation();
        boolean test = false;
        if (test) {
            nonRandomCards();
        }
        else {
            readNumberOfCards();

            createOffensiveCard("hit", 52,"/resources/cards/hit.png", 10, TypeOfOffensive.HIT);
            createOffensiveCard("disarm", 12,"/resources/cards/disarm.png", 11, TypeOfOffensive.DISARM);
            createOffensiveCard("theft", 12,"/resources/cards/theft.png", 12, TypeOfOffensive.THEFT);
            createDefensiveCard("block", 20, "/resources/cards/block.png", 17,TypeOfOffensive.HIT);
            createDefensiveCard("defense", 10, "/resources/cards/defense.png", 16, TypeOfOffensive.THEFT, TypeOfOffensive.DISARM);
            createHealCard("single potion", 10, "/resources/cards/singlePotion.png", 13, 20, TypeOfHeal.SELF_HEAL);
            createHealCard("team potion", 8, "/resources/cards/teamPotion.png", 14, 15, TypeOfHeal.TEAM_HEAL);
            createHealCard("kettle", 5, "/resources/cards/kettle.png", 15, 10, TypeOfHeal.KETTLE);
            createWeapons();
            createShields();
            createHelms();
            Collections.shuffle(cards);
        }
        this.unPickedCards = cards.size();
    }

    private void readNumberOfCards() {
        try {
            URL fileUrl = new URL(urlOfFolder, "Files/number_of_cards.txt", null);
            File fileNumOfCards = Paths.get(fileUrl.toURI()).toFile();
            Scanner scanner = new Scanner(fileNumOfCards);
            while (scanner.hasNextLine()) {
                final String separator = " : ";
                String line = scanner.nextLine();
                if (line.contains(separator)) {
                    String[] input = line.split(separator);
                    try {
                        int numberOfCard = Integer.parseInt(input[1]);
                        String nameOfCard = input[0];
                        numberOfCards.put(nameOfCard, numberOfCard);
                    } catch (Exception e) {
                        System.err.println("Wrong input format: " + line);
                    }
                }
                else {
                    System.err.println("Wrong input format: " + line);
                }
            }
        } catch (Exception io) {
            System.err.println("File for number of cards not found.");
        }
    }

    private void createOffensiveCard(String name, int defaultNum, String path, int value, TypeOfOffensive type) {
        final int numOfCards = numberOfCards.getOrDefault(name, defaultNum);
        for (int i = 0; i < numOfCards; i++) {
            OffensiveCard offensiveCard = new OffensiveCard(name, path, generateId(), value, type);
            insert(offensiveCard);
        }
    }

    private void createDefensiveCard(String name, int defaultNum, String path, int value, TypeOfOffensive... against) {
        final int numOfCards = numberOfCards.getOrDefault(name, defaultNum);
        for (int i = 0; i < numOfCards; i++) {
            Card block = new DefensiveCard(name, path, generateId(), value, Arrays.asList(against));
            insert(block);
        }
    }

    private void createHealCard(String name, int defaultNum, String path, int value, int heal, TypeOfHeal type) {
        final int numOfCards  = numberOfCards.getOrDefault(name, defaultNum);
        for (int i = 0; i < numOfCards; i++) {
            HealCard healCard = new HealCard(name, path, generateId(), value, heal, type);
            insert(healCard);
        }
    }

    private void readUselessLines(Scanner scanner, int numberOfLines) {
        for (int headerLine = 0; headerLine < numberOfLines; headerLine++) {
            scanner.nextLine();
        }
    }

    private void readWeaponStats() {
        try {
            URL fileUrl = new URL(urlOfFolder, "Files/weapons.txt", null);
            File weapons = Paths.get(fileUrl.toURI()).toFile();
            Scanner scanner = new Scanner(weapons);
            readUselessLines(scanner, 4); // hlavicka suboru
            final String separator = " : ";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] input = line.split(separator);
                try {
                    String name = input[0];
                    int range = Integer.parseInt(input[1]);
                    int damage = Integer.parseInt(input[2]);
                    weaponRanges.put(name, range);
                    weaponDamage.put(name, damage);
                } catch (Exception e) {
                    System.err.println("Bad input in file with weapons. Input: " + line);
                }
            }
        } catch (Exception io) {
            System.err.println("File with weapon stats not found");
        }
    }

    private void createWeapons() {
        readWeaponStats();
        createWeapon("axe",3, "/resources/cards/axe.png", 2,2, 30);
        createWeapon("mace",3, "/resources/cards/mace.png", 2,1, 40);
        createWeapon("dagger",5, "/resources/cards/dagger.png", 1,3, 25);
        createWeapon("sword",3, "/resources/cards/sword.png", 2,3, 25);
        createWeapon("bow",3, "/resources/cards/bow.png", 1,9, 20);
        createWeapon("hammer",2, "/resources/cards/hammer.png", 3,5, 40);
    }

    private void createWeapon(String name, int defaultNum, String path, int value, int defaultRange, int defaultDmg) {
        int numOfCard = numberOfCards.getOrDefault(name, defaultNum);
        int range = weaponRanges.getOrDefault(name, defaultRange);
        int damage = weaponDamage.getOrDefault(name, defaultDmg);
        for (int i = 0; i < numOfCard; i++) {
            Weapon weapon = new Weapon(name, path, generateId(),value, range, damage);
            insert(weapon);
        }
    }

    private void readShieldsStats() {
        try {
            URL fileUrl = new URL(urlOfFolder, "Files/shields.txt", null);
            File shields =  Paths.get(fileUrl.toURI()).toFile();
            Scanner scanner = new Scanner(shields);
            readUselessLines(scanner, 4); // hlavicka suboru
            final String separator = " : ";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] input = line.split(separator);
                try {
                    String name = input[0];
                    int protection = Integer.parseInt(input[1]);
                    shieldProtections.put(name, protection);
                } catch (Exception e) {
                    System.err.println("Bad input in file with shields. Input: " + line);
                }
            }
        } catch (Exception io) {
            System.err.println("File with shield stats not found");
        }
    }

    private void createShields() {
        readShieldsStats();
        createShield("wooden shield", 6, "/resources/cards/woodenShield.png", 1, 5);
        createShield("iron shield", 4, "/resources/cards/ironShield.png", 2, 10);
        createShield("golden shield", 2, "/resources/cards/goldenShield.png", 3, 15);
    }

    private void createShield (String name, int defaultNum, String path, int value, int defaultProtection) {
        int numOfShields = numberOfCards.getOrDefault(name, defaultNum);
        int protection = shieldProtections.getOrDefault(name, defaultProtection);
        for (int i = 0; i < numOfShields; i++) {
            Shield shield = new Shield(name, path, generateId(),value, protection);
            insert(shield);
        }
    }

    private void readHelmStats() {
        try {
            URL fileUrl = new URL(urlOfFolder, "Files/helms.txt", null);
            File helms =  Paths.get(fileUrl.toURI()).toFile();
            Scanner scanner = new Scanner(helms);
            readUselessLines(scanner, 4); // hlavicka suboru
            final String separator = " : ";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] input = line.split(separator);
                try {
                    String name = input[0];
                    int rangePlus = Integer.parseInt(input[1]);
                    int rangeMinus = Integer.parseInt(input[2]);
                    helmPlusRange.put(name, rangePlus);
                    helmMinusRange.put(name, rangeMinus);
                } catch (Exception e) {
                    System.err.println("Bad input in file with helms. Input: " + line);
                }
            }
        } catch (Exception io) {
            System.err.println("File with helms stats not found");
        }
    }

    private void createHelms() {
        readHelmStats();
        createHelm("spartan helm", 4, "/resources/cards/spartanHelm.png", 1, 1, 0);
        createHelm("knight helm", 4, "/resources/cards/knightHelm.png", 2, 0, 1);
        createHelm("viking helm", 2, "/resources/cards/vikingHelm.png", 3, 1, 1);
    }

    private void createHelm(String name, int defaultNum, String path, int value, int defaultRangePlus, int defaultRangeMinus) {
        int numOfCard = numberOfCards.getOrDefault(name, defaultNum);
        int rangePlus = helmPlusRange.getOrDefault(name, defaultRangePlus);
        int rangeMinus = helmMinusRange.getOrDefault(name, defaultRangeMinus);
        for (int i = 0; i < numOfCard; i++) {
            Helm helm = new Helm(name, path, generateId(),value, rangePlus, rangeMinus);
            insert(helm);
        }
    }

    private void addNonRandomHits(int numberOfHits) {
        for (int i = 0; i < numberOfHits; i++) {
            OffensiveCard hit = new OffensiveCard("hit", "\\Images\\cards\\hit.png", generateId(), 0, TypeOfOffensive.HIT);
            insert(hit);
        }
    }

    private void nonRandomCards() {
        final int numberOfDisarms = 100;
        for (int i = 0; i < numberOfDisarms; i++) {
            OffensiveCard disarm = new OffensiveCard("disarm", "\\Images\\cards\\disarm.png", generateId(), 11, TypeOfOffensive.DISARM);
            insert(disarm);
        }

        int numOfBows = 100;
        for (int i = 0; i < numOfBows; i++) {
            Weapon bow = new Weapon("bow", "\\Images\\cards\\bow.png", generateId(), 1,9, 20);
            insert(bow);
        }
        Collections.shuffle(cards);
    }

    private void demonstration() {
        // hrac 1
        addNonRandomHits(3);

        // hrac 2
        List<TypeOfOffensive> againstHit = new ArrayList<>();
        againstHit.add(TypeOfOffensive.HIT);
        Card block = new DefensiveCard("block", "\\Images\\cards\\block.png", generateId(), 0, againstHit);
        insert(block);

        addNonRandomHits(2);

        // hrac 3
        Helm spartanHelm = new Helm("spartan helm", "\\Images\\cards\\spartanHelm.png", generateId(),1,1, 0);
        insert(spartanHelm);

        List<TypeOfOffensive> againstDisarm = new ArrayList<>();
        againstDisarm.add(TypeOfOffensive.THEFT);
        againstDisarm.add(TypeOfOffensive.DISARM);
        DefensiveCard defense = new DefensiveCard("defense", "\\Images\\cards\\defense.png", generateId(), 0, againstDisarm);
        insert(defense);

        Card block2 = new DefensiveCard("block", "\\Images\\cards\\block.png", generateId(), 0, againstHit);
        insert(block2);


        // hrac 4
        OffensiveCard theft1 = new OffensiveCard("theft", "\\Images\\cards\\theft.png", generateId(), 0, TypeOfOffensive.THEFT);
        insert(theft1);
        OffensiveCard theft2 = new OffensiveCard("theft", "\\Images\\cards\\theft.png", generateId(), 0, TypeOfOffensive.THEFT);
        insert(theft2);
        OffensiveCard disarm2 = new OffensiveCard("disarm", "\\Images\\cards\\disarm.png", generateId(), 0, TypeOfOffensive.DISARM);
        insert(disarm2);

        // hrac 1 prvy pick
        Shield golden = new Shield("golden shield", "\\Images\\cards\\goldenShield.png", generateId(),3, 10);
        insert(golden);
        Weapon hammer = new Weapon("hammer", "\\Images\\cards\\hammer.png", generateId(),3, 6, 40);
        insert(hammer);

        // hrac 2 prvy pick
        HealCard singlePotion = new HealCard("single potion", "\\Images\\cards\\singlePotion.png", generateId(), 0,20, TypeOfHeal.SELF_HEAL);
        insert(singlePotion);

        OffensiveCard disarm = new OffensiveCard("disarm", "\\Images\\cards\\disarm.png", generateId(), 0, TypeOfOffensive.DISARM);
        insert(disarm);

        // hrac 3, prvy pick
        Card block3 = new DefensiveCard("block", "\\Images\\cards\\block.png", generateId(), 0, againstHit);
        insert(block2);
        Card block4 = new DefensiveCard("block", "\\Images\\cards\\block.png", generateId(), 0, againstHit);
        insert(block2);

        // hrac 4, prvy pick
        Card block5 = new DefensiveCard("block", "\\Images\\cards\\block.png", generateId(), 0, againstHit);
        insert(block2);

        HealCard teamPotion = new HealCard("teamPotion", "Images\\cards\\teamPotion.png", generateId(),0,  15, TypeOfHeal.TEAM_HEAL);
        insert(teamPotion);


        // hrac 1, druhy pick
        addNonRandomHits(2);

        // hrac 3, druhy pick
        Shield iron = new Shield("iron shield", "Images\\cards\\ironShield.png",generateId(), 2,5);
        insert(iron);

        Shield wooden = new Shield("wooden shield", "Images\\cards\\woodenShield.png", generateId(),1, 3);
        insert(wooden);

        // hrac 4, druhy pick
        OffensiveCard theft3 = new OffensiveCard("theft", "Images\\cards\\theft.png", generateId(), 0, TypeOfOffensive.THEFT);
        insert(theft1);
        addNonRandomHits(1);

        // hrac 1, treti pick
        HealCard kettle = new HealCard("kettle", "Images\\cards\\kettle.png", generateId(),0,  10, TypeOfHeal.KETTLE);
        insert(kettle);
        addNonRandomHits(1);

        // hrac 2, treti pick
        Weapon axe = new Weapon("axe", "Images\\cards\\axe.png", generateId(),2, 2, 30);
        insert(axe);
        addNonRandomHits(1);

        //rest
        addNonRandomHits(80);

    }

    public Card pick() {
        unPickedCards--;
        if (unPickedCards <= 0) {
            unPickedCards = cards.size();
            Collections.shuffle(cards);
        }
        if (cards.isEmpty()) {
            System.err.println("No cards in deck!!! ");
            Card emergency = new OffensiveCard("attack", "/resources/hit.png",generateId(),0, TypeOfOffensive.HIT);
            return emergency;
        }
        else {
            return cards.removeFirst(); // O(1) zlozitost
        }
    }

    private int generateId() {
        return cards.size() + 1;
    }

    public void insert(Card card) {
        card.reset();
        cards.addLast(card);
    }
}
