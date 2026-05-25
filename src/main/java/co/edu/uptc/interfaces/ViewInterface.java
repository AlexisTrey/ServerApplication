package co.edu.uptc.interfaces;

import co.edu.uptc.dto.PlayerDto;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;

import java.util.List;

public interface ViewInterface {

    void setPresenter(PresenterInterface presenter);
    void start();
    void refresh();
    void updateGameState(List<PlayerDto> players);
    void updatePlayerList(List<Player> players);
    void setGameStatus(GameStatus status);

}
