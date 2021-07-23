package GamePlay.Players;

import GamePlay.GameChange;
import GamePlay.GameChangeMsg;
import GamePlay.Steal;
import GamePlay.Cards.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Bot extends Player {

    private int addedHealth;

    public Bot(String name, int id) {
        super(name);
        setId(id);
        this.addedHealth = 0;
    }


    public Bot(String name, int id, boolean testing) {
        super(name, testing);
        setId(id);
        this.addedHealth = 0;
    }



    public Bot(Player player) {
        super(player);
    }

    public void setAddedHealth(int addedHealth) {
        this.addedHealth = addedHealth;
    }

    public int getAddedHealth() {
        return addedHealth;
    }

    protected void delay() {
        if (! this.isTesting()) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException i) {
                i.printStackTrace();
            }
        }
    }

    protected <E> E randomElement( List<E> elements) {
        if (elements.isEmpty()) {
            return null;
        }
        else {
            Random generator = new Random();
            int randomIndex = generator.nextInt(elements.size());
            return elements.get(randomIndex);
        }
    }

    @Override
    public String getType() {
        return "bot";
    }

    @Override
    public void pickCard() throws IOException {
        animationDelay();
        Card pickedCard = deck.pick();
        pickedCard.setOwner(this);
        addToInventory(pickedCard);
        GameChangeMsg infoMsg = new GameChangeMsg(GameChange.PICK_CARD, this);
        broadcastToOpponents(infoMsg);
    }

    @Override
    public final void defense(OffensiveCard offensiveCard) throws IOException, ClassNotFoundException {
        delay();
        DefensiveCard defensiveCard = findDefensiveCard(offensiveCard);
        if (defensiveCard != null) {
            // mam nejaku obrannu kartu a tu aj pouzijem
            broadcastToOpponents(defensiveCard);
            animationDelay();
            throwUsedCard(defensiveCard);
        }
        else if (offensiveCard.getType() == TypeOfOffensive.HIT) {
            // nemam obrannu kartu stracam zivoty
            Health myHealth = getHealth();
            int takenHealth = offensiveCard.getDamage() - this.getProtection();
            if (takenHealth < 0) {
                takenHealth = 0;
            }
            myHealth.changeHealth(- takenHealth);
            broadcastToOpponents(myHealth);
        }
        else if ((offensiveCard.getType() == TypeOfOffensive.DISARM || offensiveCard.getType() == TypeOfOffensive.THEFT)
                && offensiveCard.getTarget() != null) {
            //nemam obrannu kartu pridem o oktivny item
            Item target = offensiveCard.getTarget();
            Item removedItem = removeItem(target.positionIndex()); // odober item
            ignoreAttack(offensiveCard, removedItem);
        }
        else if ((offensiveCard.getType() == TypeOfOffensive.DISARM || offensiveCard.getType() == TypeOfOffensive.THEFT)
                && offensiveCard.getTarget() == null){
            // nemam obrannu kartu pridem o kartu z inventara
            //TODO toto by sa mohlo spravit trocha beźpecnejsie, ak nemam ziadne karty tak sa nic nevykona
            //TODO nevyhoda by bola v tom ze taky pripad bz nikdy nemal nastat a ja budem skutocnu chybu iba maskovat
            Card randomCard = randomElement(getInventory());
            removeFromInventory(randomCard);
            ignoreAttack(offensiveCard, randomCard);
        }
        else {
            System.err.println("Toto by sa vykonat nemalo! (defense)");
        }
    }

    private void ignoreAttack(OffensiveCard offensiveCard, Card lostCard) throws IOException{
        if (offensiveCard.getType() == TypeOfOffensive.DISARM) {
            lostCard.setThrown(true);
            broadcastToOpponents(lostCard); // informuj klientov o zmene
            animationDelay();
            deck.insert(lostCard);
        }
        else if (offensiveCard.getType() == TypeOfOffensive.THEFT){
            Player thief = serverPlayer(offensiveCard.getOwner());
            Steal steal = new Steal(lostCard, thief);
            broadcastToOpponents(steal);
            animationDelay();
            lostCard.setOwner(thief); // karta bola ukradnut tymto hracom takze je to novy majitel
            thief.addToInventory(lostCard);
        }
        else {
            System.err.println("Toto by sa vykonat nemalo! (ignoreAttack)");
        }
    }

    private DefensiveCard findDefensiveCard(OffensiveCard offensiveCard) {
        DefensiveCard myDefense = null;
        for (Card card : getInventory()) {
            if (card instanceof DefensiveCard) {
                DefensiveCard potentialDefense = (DefensiveCard) card;
                if (potentialDefense.against(offensiveCard.getType())) {
                    myDefense = potentialDefense; // nasiel som kartu ktorou sa mozem branit
                    break;
                }
            }
        }
        return myDefense; // ak je null tak sa nedokazem branit
    }

    protected List<Player> enemies() {
        List<Player> enemies = new ArrayList<>();
        for (Player player : players) {
            if (! player.getTeam().equals(this.getTeam())) {
                enemies.add(player);
            }
        }
        return enemies;
    }

    protected List<Player> enemiesInRange() {
       List<Player> enemiesInRange = new ArrayList<>();
       for (Player enemy : enemies()) {
           if (playerIsInRange(this, enemy)) {
               enemiesInRange.add(enemy);
           }
       }
       return enemiesInRange;
    }

    protected List<Player> enemiesWithCards() {
        List<Player> enemiesWithCards = new ArrayList<>();
        for (Player enemy : enemies()) {
            if (! enemy.getInventory().isEmpty() || (enemy.getFirstActiveItem() != null)) {
                enemiesWithCards.add(enemy);
            }
        }
        return enemiesWithCards;
    }

    private boolean playerIsInRange(Player attacker, Player defender) {
        int attackerPosition = players.indexOf(attacker);
        int defenderPosition = players.indexOf(defender);
        int distanceLeft = 0;
        while (attackerPosition != defenderPosition) { // posupne iterujem jednotlive pozicie
            distanceLeft++;
            attackerPosition--;
            if (attackerPosition < 0) {
                attackerPosition = players.size() -1;
            }
        }
        int distanceRight = players.size() - distanceLeft;
        int shortestDistance = Math.min(distanceLeft, distanceRight);
        int myRange = attacker.getOffensiveRange() - defender.getDefensiveRange();
        return (myRange >= shortestDistance);
    }

    protected abstract void throwCards() throws IOException;

    protected  void useSingleHeal() throws IOException {
        Card card = getCard(TypeOfHeal.SELF_HEAL);
        while (card != null) {
            HealCard singleHeal = (HealCard) card;
            if (shouldUseSingleHeal(singleHeal)) {
                delay();
                healPlayer(this, singleHeal);
                card = getCard(TypeOfHeal.SELF_HEAL);
            }
            else {
                break;
            }
        }
    }

    protected abstract boolean shouldUseSingleHeal(HealCard healCard);

    protected void useTeamHeal() throws IOException {
        Card card = getCard(TypeOfHeal.TEAM_HEAL);
        while (card != null) {
            HealCard teamHeal = (HealCard) card;
            Player teammate = teammateForHeal(teamHeal);
            if (teammate != null) {
                delay();
                teamHeal.setTarget(teammate);
                healPlayer(teammate, teamHeal);
                card = getCard(TypeOfHeal.TEAM_HEAL);
            } else {
                break;
            }
        }
    }

    protected abstract Player teammateForHeal(HealCard healCard);

    protected void useKettle() throws IOException{
        Card card = getCard(TypeOfHeal.KETTLE);
        while (card != null) {
            HealCard kettle = (HealCard) card;
            Health myHealth = getHealth();
            if (shouldUseKettle(kettle)) {
                delay();
                List<Player> teammates = getTeammates();
                for (Player teammate : teammates) {
                    Health teammateHealth = teammate.getHealth();
                    teammateHealth.changeHealth(kettle.getHeal());
                }
                broadcastToOpponents(kettle);
                throwUsedCard(kettle);
                card = getCard(TypeOfHeal.KETTLE);
            }
            else {
                break;
            }
        }
    }

    protected abstract boolean shouldUseKettle(HealCard kettle);

    protected void healPlayer(Player player, HealCard heal) throws IOException{
        Health teammateHealth = player.getHealth();
        teammateHealth.changeHealth(heal.getHeal());
        broadcastToOpponents(heal);
        delay();
        throwUsedCard(heal);
    }

    @Override
    protected void equip() throws IOException {
        for (int itemIndex = 0; itemIndex < 3; itemIndex++) {
            Item alternative = bestItemInInventory(itemIndex);
            if (alternative != null) { // existuje alternativa
                Item actual = getItem(itemIndex);
                if (actual == null) {
                    addItem(alternative);
                    removeFromInventory(alternative);
                    alternative.setActive(true);
                    broadcastToOpponents(alternative);
                    delay();
                }
                else if (alternative.getValue() > actual.getValue()) {
                    alternative.setActive(true); // alternativn item bude moj aktivny item
                    removeFromInventory(alternative); // treba ho odstranit z inventaru
                    Item removed = addItem(alternative); // dostane predchadzajuci item
                    removed.setActive(false); // predchadzajuci item uz nie je viacej aktivny
                    addToInventory(removed); // pridame stary item do inventaru
                    broadcastToOpponents(alternative);
                    delay();
                }
                else {
                    // zochovavam sucastny stav,
                    // cize mam nejaku alternativu ale ta alternativa je horsia ako item ktory mam momentalne nasadeny
                }
            }
        }
    }



    private Item bestItemInInventory(int itemIndex) {
        Item bestItem = null;
        for (Card card : getInventory()) {
            if (card instanceof Item) {
                Item item = (Item) card;
                if (item.positionIndex() == itemIndex) {
                    if (bestItem == null || item.getValue() > bestItem.getValue()) {
                        bestItem = item;
                    }
                }
            }
        }
        return bestItem;
    }



    protected Card getCard(TypeOfHeal typeOfHeal) {
        for (Card myCard : getInventory()) {
            if (myCard instanceof HealCard) {
                HealCard myHeal = (HealCard) myCard;
                if (myHeal.getType() == typeOfHeal) {
                    return myHeal;
                }
            }
        }
        return null;
    }

    protected Card getCard(TypeOfOffensive typeOfOffensive) {
        for (Card myCard : getInventory()) {
            if (myCard instanceof OffensiveCard) {
                OffensiveCard offensiveCard = (OffensiveCard) myCard;
                if (offensiveCard.getType() == typeOfOffensive) {
                    return offensiveCard;
                }
            }
        }
        return null;
    }

    protected void offenseWithHit(OffensiveCard offensiveCard) throws IOException, ClassNotFoundException{
        offensiveCard.setDamage(getDamage());
        Player defender = offensiveCard.getDefender();
        broadcastToOpponents(offensiveCard, defender);
        animationDelay();
        delay();
        defender.defense(offensiveCard);
        throwUsedCard(offensiveCard);
    }

    protected void offenseWithTheftOrDisarm(OffensiveCard offensiveCard) throws IOException, ClassNotFoundException{
        Player defender = offensiveCard.getDefender();
        Item defenderItem = defender.getFirstActiveItem();
        if (defenderItem != null) { // ak sme nasli nejaky item tak na neho zautocime
            offensiveCard.setTarget(defenderItem);
        }
        broadcastToOpponents(offensiveCard, defender);
        animationDelay();
        delay();
        defender.defense(offensiveCard);
        throwUsedCard(offensiveCard);
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (super.equals(obj) && obj instanceof Bot) {
//            Bot bot = (Bot) obj;
//            return bot.getName().equals(this.getName()) && (bot.getId() == this.getId() && bot.getType().equals(this.getType()));
//        }
//        else {
//            return false;
//        }
//    }
}
