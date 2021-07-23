package GamePlay.Players;

import Server.Connection;
import GamePlay.GameChange;
import GamePlay.GameChangeMsg;
import GamePlay.Steal;
import GamePlay.Cards.*;

import java.io.IOException;

public class Human extends Player{

    private OffensiveCard offensiveCard;
    private HealCard healCard;
    private Item item;

    public Human(String name) {
        super(name);
        healCard = null;
        item = null;
        offensiveCard = null;
    }

    @Override
    public String getType() {
        return "human";
    }

    @Override
    public boolean move() throws IOException, ClassNotFoundException {
        Connection myConnection = opponents.get(this);
        if ((myConnection != null) && myConnection.isConnected()) {
            broadcastToOpponents(new GameChangeMsg(GameChange.START_MOVE, this));
            Object message;
            try {
                message = myConnection.receiveMessage();
                myConnection.sendMessage(GameChange.STOP_COMMUNICATION);
            } catch (Exception disconnected) {
                replaceByBot();
                Player replacement = serverPlayer(this);
                replacement.move();
                return false;
            }
            //System.out.println("received: " + message + " from " + client.getPlayer().getName());
            if (message instanceof GameChangeMsg) {
                GameChangeMsg gameChange = (GameChangeMsg) message;
                switch (gameChange.getChange()) {
                    case END_MOVE: {
                        // ma svoje opodstatnenie pri zahadzovani prebytocnych kariet
                        // ked zahadzujem karty tak je potrebne endMoveBtn expicitne schopat po skonceni tahu
                        // zaslanim spravy END_MOVE klient skrtyje endMoveBtn a prerusi komunikaciu
                        try {
                            myConnection.sendMessage(GameChange.END_MOVE);
                        } catch (Exception e) {
                            return false;
                        }
                        return false; // koniec tahu
                    }
                }
            }
            else if (message instanceof Card) {
                Card card = (Card) message;
                if (card.isThrown()) {
                    broadcastToOpponents(card, this);
                    throwUsedCard(card);
                } else if (card instanceof OffensiveCard) {
                    offensiveCard = (OffensiveCard) card;
                    offense();
                } else if (card instanceof HealCard) {
                    healCard = (HealCard) card;
                    healing();
                } else if (card instanceof Item) {
                    item = (Item) card;
                    equip();
                }
            }
            return true; // pokracovanie tahu

        }
        else {
            return false; // hrac je odpojeny neurobim ziaden tah
        }
    }


    @Override
    protected void healing() throws IOException {
        broadcastToOpponents(healCard, this);

        if (healCard.getType() == TypeOfHeal.SELF_HEAL) {
            this.getHealth().changeHealth(+healCard.getHeal());
        }
        else if (healCard.getType() == TypeOfHeal.TEAM_HEAL){
            Player healed = serverPlayer(healCard.getTarget());
            healed.getHealth().changeHealth(+healCard.getHeal());
        }
        else if (healCard.getType() == TypeOfHeal.KETTLE) {
            for (Player player : getTeammates()) {
                player.getHealth().changeHealth(+healCard.getHeal());
            }
        }
        throwUsedCard(healCard);
        healCard = null;
    }

    @Override
    protected boolean offense() throws IOException, ClassNotFoundException {
        Human attacker = this;
        Player defender = serverPlayer(offensiveCard.getDefender());
        broadcastToOpponents(offensiveCard, attacker, defender);
        defender.defense(offensiveCard);
        throwUsedCard(offensiveCard);
        return false;
    }

    @Override
    public void pickCard() throws IOException {
        animationDelay();
        Card pickedCard = deck.pick();
        pickedCard.setOwner(this);
        addToInventory(pickedCard);
        Connection clientConn =  opponents.get(this);
        if (clientConn != null && clientConn.isConnected()) {
            clientConn.sendMessage(pickedCard);
        }
        GameChangeMsg infoMsg = new GameChangeMsg(GameChange.PICK_CARD, this);
        broadcastToOpponents(infoMsg, this);
    }

    @Override
    public void defense(OffensiveCard offensiveCard) throws IOException, ClassNotFoundException {
        Human defender = this;
        Connection myConn = opponents.get(defender);
        if (myConn != null && myConn.isConnected()) {
            Object response;
            try {
                myConn.sendMessage(offensiveCard);
                myConn.sendMessage(GameChange.START_COMMUNICATION);
                response = myConn.receiveMessage();
                myConn.sendMessage(GameChange.STOP_COMMUNICATION);
            } catch (Exception disconnected) {
                replaceByBot();
                Player replacement = serverPlayer(this);
                replacement.defense(offensiveCard);
                return;
            }

            boolean throwActiveItem = (response instanceof Item && ((Item) response).isActive());
            if (throwActiveItem) {
                Item item = (Item) response;
                broadcastToOpponents(item, defender);
                Item removed = removeItem(item.positionIndex());
                throwUsedCard(removed);
            } else if (response instanceof Card) {
                Card card = (Card) response;
                broadcastToOpponents(card, defender);
                throwUsedCard(card);
            } else if (response instanceof Health) {
                Health health = (Health) response;
                broadcastToOpponents(health, defender);
                setHealth(health);
            } else if (response instanceof Steal) {
                Steal steal = (Steal) response;
                broadcastToOpponents(steal);
                someoneStealMyCard(steal);
            }
        }
        else {
            replaceByBot();
            Player replacement = serverPlayer(this);
            replacement.defense(offensiveCard);
        }
    }

    private void replaceByBot() {
        int position = players.indexOf(this);
        if (position != -1) {
            players.remove(this);
            Player bot = new MediumBot(this);
            players.add(position, bot);
            opponents.remove(this);
        }
    }

    @Override
    protected void equip() throws IOException {
        broadcastToOpponents(item, this);
        removeFromInventory(item); // nasadzujem si item takze ide prec z inventara
        Item oldItem = addItem(item); // item ktory som mal doteraz nasadeny sa vrati ako navratova hodnota
        if (oldItem != null) { // ci som mal predtym nasadeny nejaky item
            addToInventory(oldItem); // stary item si ulozim do inventara
        }
        item = null;
    }

    /***
     * Ked mi niekto ukradne kartu tak tato metoda je zodpovedna za upravu stavu obrancu aj utocnika
     * @param steal
     */
    private void someoneStealMyCard(Steal steal) {
        Player defender = this;
        Player attacker = serverPlayer(steal.getThief());
        Card stolen = steal.getStolen();
        if (stolen instanceof Item && ((Item) stolen).isActive()) {
            // ak to je aktivny item tak to treba zobrat z itemov a nie z inventaru
            Item stolenActiveItem = (Item) stolen;
            defender.removeItem(stolenActiveItem.positionIndex());
        }
        else {
            defender.removeFromInventory(stolen);
        }
        stolen.setOwner(attacker);
        attacker.addToInventory(stolen);
    }




//    @Override
//    public boolean equals(Object obj) {
//        return (obj instanceof Human && super.equals(obj));
//    }


}
