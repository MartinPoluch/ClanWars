package GamePlay.Players;

import GamePlay.Cards.HealCard;
import GamePlay.Cards.Item;
import GamePlay.Cards.OffensiveCard;
import GamePlay.Cards.Shield;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HardBot extends MediumBot {

    private final int BONUS_HEALTH = 20;

    public HardBot(int id) {
        super(id, "HardBot " + id);
        setId(id);
        setAddedHealth(BONUS_HEALTH);
    }

    public HardBot(int id, boolean testing) {
        super("HardBot " + id, id, testing);
        setAddedHealth(BONUS_HEALTH);
    }



    @Override
    protected Player teammateForHeal(HealCard healCard) {
        List<Player> possibleTeammates = new ArrayList<>();
        for (Player teammate : getTeammates()) {
            if (teammate.getHealth().getActualHealth() + healCard.getHeal() <= teammate.getHealth().getMaxHealth()) {
                possibleTeammates.add(teammate);
            }
        }

        Player weakest = null;
        for (Player possibleTeammate : possibleTeammates) {
            if (weakest == null || powerOfPlayer(possibleTeammate) < powerOfPlayer(weakest)) {
                weakest = possibleTeammate;
            }
        }
        return weakest;
    }

    protected int powerOfPlayer(Player player) {
        int power = 0;
        power += player.getHealth().getActualHealth() * 10;

        final int VALUE_OF_CARD = 10;
        power += player.getInventory().size() * VALUE_OF_CARD;
        power += player.getDamage() * 2;
        power += player.getProtection() * 35;
        power += player.getOffensiveRange();
        power += player.getDefensiveRange();
        return power;
    }

}
