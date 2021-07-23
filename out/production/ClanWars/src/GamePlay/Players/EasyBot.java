package GamePlay.Players;

import GamePlay.GameChange;
import GamePlay.GameChangeMsg;
import GamePlay.Cards.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EasyBot extends Bot {

    public EasyBot(int id) {
        super("EasyBot " + id, id);
    }

    public EasyBot(int id, boolean testing) {
        super("EasyBot " + id, id, testing);
    }

    @Override
    public boolean move() throws IOException, ClassNotFoundException {
        broadcastToOpponents(new GameChangeMsg(GameChange.START_MOVE, this));
        delay();
        equip();
        if (offense()) {
            return true; // bol eliminovany hrac treba urobit zmenu
        }
        healing();
        throwCards();
        delay();
        return false;
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    protected boolean offense() throws IOException, ClassNotFoundException{
        OffensiveCard theft = (OffensiveCard) getCard(TypeOfOffensive.THEFT);
        while (theft != null) { // pokial mam takuto kartu
            List<Player> enemiesForTheft = enemiesInRange(); // ziskame vsetkych nepriatelov v dosahu
            enemiesForTheft.retainAll(enemiesWithCards()); // ponechame len tych ktory maju karty
            if (enemiesForTheft.isEmpty()) {
                break;
            }
            else {
                Player enemy = randomElement(enemiesForTheft); // vyberieme nahodneho nepriatela
                theft.setDefender(enemy);
                offenseWithTheftOrDisarm(theft);
                equip(); // ziskal som novu kartu mozno mam nejaky novy item ktory si mozem nasadit
            }
            theft = (OffensiveCard) getCard(TypeOfOffensive.THEFT);
        }

        OffensiveCard disarm = (OffensiveCard) getCard(TypeOfOffensive.DISARM);
        while (disarm != null) { // pokial mam takuto kartu
            List<Player> enemiesForDisarm = enemiesWithCards();
            if (enemiesForDisarm.isEmpty()) {
                break;
            }
            else {
                Player enemy = randomElement(enemiesForDisarm); // vyberieme nahodneho nepriatela
                disarm.setDefender(enemy);
                offenseWithTheftOrDisarm(disarm);
            }
            disarm = (OffensiveCard) getCard(TypeOfOffensive.DISARM);
        }

        OffensiveCard hit = (OffensiveCard) getCard(TypeOfOffensive.HIT);
        while (hit != null) { // pokial mam takuto kartu
            List<Player> enemiesForHit = enemiesInRange(); // ziskame vsetkych nepriatelov v dosahu
            if (enemiesForHit.isEmpty()) {
                break;
            }
            else {
                Player enemy = randomElement(enemiesForHit); // vyberieme nahodneho nepriatela
                hit.setDefender(enemy);
                offenseWithHit(hit);
                boolean enemyWasEliminate = (enemy.getHealth().getActualHealth() <= 0);
                if (enemyWasEliminate) {
                    return true; // pocas utoku bol eliminovany hrac
                }
            }
            hit= (OffensiveCard) getCard(TypeOfOffensive.HIT);
        }
        return false;
    }

    @Override
    protected void throwCards() throws IOException{
        while (getInventory().size() > MAX_CARDS) {
            Card removedCard = randomElement(getInventory()); // vyberiem nahodnu kartu
            removedCard.setThrown(true);
            broadcastToOpponents(removedCard); // oznamim to vsetkym klientom
            throwUsedCard(removedCard);
            delay();
            animationDelay();
        }
    }

    @Override
    protected void healing() throws IOException {
        useSingleHeal();
        useTeamHeal();
        useKettle();
    }

    @Override
    protected boolean shouldUseSingleHeal(HealCard healCard) {
        return (getHealth().getActualHealth() < getHealth().getMaxHealth());
    }

    @Override
    protected Player teammateForHeal(HealCard healCard) {
        List<Player> teammatesWithTakenHealth = new ArrayList<>();
        List<Player> teammates = getTeammates();
        for (Player teammate : teammates) {
            boolean heHasTakenHealth = (teammate.getHealth().getActualHealth() < teammate.getHealth().getMaxHealth());
            if (heHasTakenHealth) {
                teammatesWithTakenHealth.add(teammate);
            }
        }
        return randomElement(teammatesWithTakenHealth); // vrati nahodneho hraca, ak je zoznam prazdny tak null
    }

    @Override
    protected boolean shouldUseKettle(HealCard kettle) {
        return (getHealth().getActualHealth() < getHealth().getMaxHealth());
    }
}
