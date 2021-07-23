package GamePlay;

import GamePlay.Players.Player;

import java.io.Serializable;

/**
 * Trieda reprezentuje správu ktorá je posielaná medzi klientom a serverom.
 */
public class GSChange implements Serializable {

    private GameSetting gameSetting;
    private Player initiator;
    private LobbyChange lobbyChange;

    public GSChange(GameSetting gameSetting, Player initiator, LobbyChange lobbyChange) {
        this.gameSetting = gameSetting;
        this.initiator = initiator;
        this.lobbyChange = lobbyChange;
    }

    public GameSetting getGameSetting() {
        return gameSetting;
    }

    public Player getInitiator() {
        return initiator;
    }

    public LobbyChange getLobbyChange() {
        return lobbyChange;
    }

    @Override
    public String toString() {
        if (lobbyChange == LobbyChange.DESTROY) {
            return "Server destroyed game:" + getGameSetting();
        }
        else {
            return "Player " + initiator.getName() + " " + lobbyChange + " game: " + getGameSetting();
        }
    }
}
