package co.edu.uptc.model;

import co.edu.uptc.dto.PlayerDto;
import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.pojo.Direction;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;

import java.util.List;

public class ServerModel implements ModelInterface {
    @Override
    public void addPlayer(Player player) {

    }

    @Override
    public void removePlayer(String studentCode) {

    }

    @Override
    public boolean processMove(String studentCode, Direction direction) {
        return false;
    }

    @Override
    public void startGame() {

    }

    @Override
    public void endGame() {

    }

    @Override
    public void setSpeed(int speedMs) {

    }

    @Override
    public List<Player> getPlayers() {
        return List.of();
    }

    @Override
    public GameStatus getStatus() {
        return null;
    }

    @Override
    public Player findPlayer(String studentCode) {
        return null;
    }

    @Override
    public List<PlayerDto> buildGameState() {
        return List.of();
    }

    @Override
    public int getSpeed() {
        return 0;
    }
}
