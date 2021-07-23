package GamePlay.Players;

import GamePlay.GameChange;
import GamePlay.GameChangeMsg;
import GamePlay.Cards.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MediumBot extends Bot {

    public MediumBot(int id) {
        super("MediumBot " + id, id);
        setId(id);
    }

    public MediumBot(int id, boolean testing) {
        super("MediumBot " + id, id, testing);
    }


    public MediumBot(String name, int id, boolean testing) {
        super(name, id, testing);
    }

    public MediumBot(Player player) {
        super(player);
    }

    public MediumBot(int id, String name) {
        super(name, id);
        setId(id);
    }

    @Override
    public boolean move() throws IOException, ClassNotFoundException {
        broadcastToOpponents(new GameChangeMsg(GameChange.START_MOVE, this));
        delay();
        equip();
        stealBestItem();
        if (offense()){
            return true; // bol eliminovany hrac treba urobit zmenu
        }
        healing();
        throwCards();
        return false;
    }

    @Override
    protected boolean offense() throws IOException, ClassNotFoundException {
        //TODO treba zabezpecit aby bot bol schopny odhodit knigh alebo viking helmu ked nemoze utocit
        Player weakestEnemyInRange = weakestEnemyFromStrongestTeam();
        if (weakestEnemyInRange != null) {
            OffensiveCard theftCard = (OffensiveCard) getCard(TypeOfOffensive.THEFT);
            while (theftCard != null) { // pokial mam takuto kartu
                if (enemiesWithCards().contains(weakestEnemyInRange)) {
                    theftCard.setDefender(weakestEnemyInRange);
                    offenseWithTheftOrDisarm(theftCard);
                    equip();
                    theftCard = (OffensiveCard) getCard(TypeOfOffensive.THEFT);
                    delay();
                }
                else {
                    break;
                }
            }

            OffensiveCard disarmCard = (OffensiveCard) getCard(TypeOfOffensive.DISARM);
            while (disarmCard != null) { // pokial mam takuto kartu
                if (enemiesWithCards().contains(weakestEnemyInRange)) {
                    disarmCard.setDefender(weakestEnemyInRange);
                    offenseWithTheftOrDisarm(disarmCard);
                    disarmCard = (OffensiveCard) getCard(TypeOfOffensive.DISARM);
                    delay();
                }
                else {
                    break;
                }
            }

            OffensiveCard hitCard = (OffensiveCard) getCard(TypeOfOffensive.HIT);
            while (hitCard != null) {
                hitCard.setDefender(weakestEnemyInRange);
                offenseWithHit(hitCard);
                hitCard = (OffensiveCard) getCard(TypeOfOffensive.HIT);
                boolean enemyWasEliminate = (weakestEnemyInRange.getHealth().getActualHealth() <= 0);
                if (enemyWasEliminate) {
                    return true; // pocas utoku bol eliminovany hrac
                }
            }
        }
        return false;
    }



    private void stealBestItem() throws IOException, ClassNotFoundException{
        OffensiveCard theftCard = (OffensiveCard) getCard(TypeOfOffensive.THEFT);
        while (theftCard != null) {
            Item bestItem = getBestItemForMe();
            if (bestItem == null) {
                break;
            }
            else {
                Player defender = serverPlayer(bestItem.getOwner());
                theftCard.setDefender(defender);
                theftCard.setTarget(bestItem);
                broadcastToOpponents(theftCard, defender);
                defender.defense(theftCard);
                throwUsedCard(theftCard);
                equip(); // tento item si hned nasadim, ovplyvni to pouzitie metody getBestItem()
                theftCard = (OffensiveCard) getCard(TypeOfOffensive.THEFT);
            }
        }
    }

    private Item getBestItemForMe() {
        Item best = null;
        for (int indexOfItem = 0; indexOfItem < 3; indexOfItem++) {
            for (Player enemy : enemiesInRange()) {
                Item possibleBest = enemy.getItem(indexOfItem);
                if (possibleBest != null) {
                    boolean betterItem = ((best == null) || (possibleBest.getValue() > best.getValue()));
                    if (betterItem) {
                        best = possibleBest;
                    }
                }
            }
            if (best != null) {
                Item myActualItem = getItem(indexOfItem);
                boolean bestIsBetterThenMyActual = ((myActualItem == null) || (best.getValue() > myActualItem.getValue()));
                if (bestIsBetterThenMyActual) {
                    return best;
                }
            }

        }
        return null;
    }

    private Player weakestEnemyFromStrongestTeam() {
        Player weakest = null;
        for (Player enemy : enemiesFromStrongestTeam()) {
            if ((weakest == null) || (powerOfPlayer(enemy) < powerOfPlayer(weakest))) {
                weakest = enemy;
            }
        }
        return weakest;
    }

    protected int powerOfPlayer(Player player) {
        return player.getHealth().getActualHealth();
    }

    private List<Player> enemiesFromStrongestTeam() {
        List<Player> enemiesFromStrongestTeam = new ArrayList<>();
        Team strongest = strongestTeamInRange();
        if (strongest != null) {
            for (Player enemy : enemiesInRange()) {
                if (enemy.getTeam().equals(strongest)) {
                    enemiesFromStrongestTeam.add(enemy);
                }
            }
        }
        return enemiesFromStrongestTeam;
    }

    private Team strongestTeamInRange() {
        Map<Team, Integer> teams = new HashMap<>();
        for (Player enemyInRange : enemiesInRange()) {
            Team enemiesTeam = enemyInRange.getTeam();
            int powerOfTeam = 0;
            if (! teams.containsKey(enemiesTeam)) {
                for (Player enemy : enemies()) {
                    if (enemy.getTeam().equals(enemiesTeam)) {
                        powerOfTeam += powerOfPlayer(enemy);
                    }
                }
                teams.put(enemiesTeam, powerOfTeam);
            }
        }

        Team strongest = null;
        for (Map.Entry<Team, Integer> entry : teams.entrySet()) {
            if ((strongest == null) || (teams.get(strongest) < entry.getValue())) {
                strongest = entry.getKey();
            }
        }
        return strongest;
    }


    @Override
    protected void throwCards() throws IOException{
        while (getInventory().size() > MAX_CARDS) {
            Card worst = findCardWithLowestValue();
            if (worst instanceof HealCard) {
                HealCard healCard = (HealCard) worst;
                throwHeal(healCard);
            }
            else {
                worst.setThrown(true);
                broadcastToOpponents(worst); // oznamim to vsetkym klientom
                throwUsedCard(worst);
                delay();
            }
        }
    }

    private void throwHeal(HealCard healCard) throws IOException{
        //TODO, doplnit pouzitie healu, heal by sa nemal nikdy zahodit, mal by sa pouzit
        healCard.setThrown(true);
        broadcastToOpponents(healCard); // oznamim to vsetkym klientom
        throwUsedCard(healCard);
        delay();
    }

    private Card findCardWithLowestValue() {
        Card lowest = null;
        for (Card card : getInventory()) {
            if ((lowest == null) || ( card.getValue() < lowest.getValue())){
                lowest = card;
            }
        }
        return lowest;
    }


    @Override
    protected boolean shouldUseSingleHeal(HealCard healCard) {
        return (getHealth().getActualHealth() + healCard.getHeal() <= getHealth().getMaxHealth());
    }

    /**
     * Najdeme spojenca s najmenším množstvom životov pri ktorom sa využije daný heal s maximálnou efektivitou.
     * @return spojenec s najmenším množstvom životov
     */
    @Override
    protected Player teammateForHeal(HealCard healCard) {
        Player lowestHealth = null;
        for (Player teammate : getTeammates()) {
            if (lowestHealth == null || teammate.getHealth().getActualHealth() < lowestHealth.getHealth().getActualHealth()) {
                lowestHealth = teammate;
            }
        }
        if (lowestHealth == null) {
            return null; // toto sa nikdy nevykona getTeammmatee() vzdy vrati minimalne jedneho hraca
        }
        else if (lowestHealth.getHealth().getActualHealth() + healCard.getHeal() <= lowestHealth.getHealth().getMaxHealth()) {
            return lowestHealth; // spojenenc ktory ma najmennej zivotov a zaroven sa heal vyuzije efektivne
        }
        else {
            return null;
        }

    }

    @Override
    protected boolean shouldUseKettle(HealCard kettle) {
        double percentageLimit = 0.25;
        boolean everybodyHasTakenHealth = true;
        for (Player teammate : getTeammates()) {
            Health health = teammate.getHealth();
            boolean teammateHasVeryLowHealth = (health.getActualHealth() <= health.getMaxHealth() * percentageLimit);
            if (teammateHasVeryLowHealth) {
                return true; // treba pouzit kettle aby sme sa pokusili zachranit spojenca
            }
            else if (health.getActualHealth() + kettle.getHeal() > health.getMaxHealth()) {
                everybodyHasTakenHealth = false;
            }
        }
        return everybodyHasTakenHealth;
    }

    @Override
    protected void healing() throws IOException {
        useKettle();
        useSingleHeal();
        useTeamHeal();
    }
}
