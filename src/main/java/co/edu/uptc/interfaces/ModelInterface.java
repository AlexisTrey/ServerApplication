package co.edu.uptc.interfaces;

import co.edu.uptc.dto.PlayerDto;
import co.edu.uptc.model.MoveResult;
import co.edu.uptc.pojo.Direction;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;

import java.util.List;

public interface ModelInterface {

    void addPlayer(Player player);
    void removePlayer(String studentCode);
    MoveResult processMove(String studentCode, Direction direction);
    void startGame();
    void endGame();
    void setSpeed(int speedMs);
    List<Player> getPlayers();
    GameStatus getStatus();
    Player findPlayer(String studentCode);
    List<PlayerDto> buildGameState();
    int getSpeed();

}
