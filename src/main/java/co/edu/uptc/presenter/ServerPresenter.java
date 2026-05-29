package co.edu.uptc.presenter;

import co.edu.uptc.dto.*;
import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.network.protocol.MessageParser;
import co.edu.uptc.network.protocol.Protocol;
import co.edu.uptc.network.server.ClientHandler;
import co.edu.uptc.network.server.ClientManager;
import co.edu.uptc.pojo.Direction;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;
import co.edu.uptc.util.Utilities;

import javax.swing.*;

public class ServerPresenter implements PresenterInterface {
    private ModelInterface model;
    private ViewInterface  view;

    @Override public void setModel(ModelInterface model) { this.model = model; }
    @Override public void setView(ViewInterface view)    { this.view  = view;  }

    @Override
    public void onStartGame() {
        model.startGame();
        broadcastGameStart();
        broadcastRoleAssignments();
        broadcastGameState();
        refreshView();
    }

    @Override
    public void onEndGame() {
        model.endGame();
        String json = MessageParser.toJson(
                new GameEndDto(Protocol.REASON_SERVER_DECISION));
        ClientManager.broadcast(json);
        refreshView();
    }

    @Override
    public void onSetSpeed(int speedMs) {
        model.setSpeed(speedMs);
    }

    @Override
    public void onPlayerConnect(String studentCode, ClientHandler handler) {
        if (model.getStatus() != GameStatus.WAITING) {
            handler.sendMessage(MessageParser.toJson(
                    new ConnectAckDto(false, "Partida en curso")));
            return;
        }
        if (model.findPlayer(studentCode) != null) {
            handler.sendMessage(MessageParser.toJson(
                    new ConnectAckDto(false, "Código ya registrado")));
            return;
        }
        int shortId = ClientManager.getCount();
        Player player = new Player(studentCode, shortId);
        model.addPlayer(player);
        handler.sendMessage(MessageParser.toJson(
                new ConnectAckDto(true, "Bienvenido")));
        refreshView();
    }

    @Override
    public void onPlayerDisconnect(String studentCode) {
        model.removePlayer(studentCode);
        broadcastGameState();
        refreshView();
    }

    @Override
    public void onPlayerMove(String studentCode, String direction) {
        if (model.getStatus() != GameStatus.IN_PROGRESS) return;
        Direction dir = Direction.valueOf(direction);
        model.processMove(studentCode, dir);
        broadcastGameState();
        checkAllDone();
        refreshView();
    }

    @Override
    public void refreshView() {
        SwingUtilities.invokeLater(() -> {
            view.updateGameState(model.buildGameState());
            view.updatePlayerList(model.getPlayers());
            view.setGameStatus(model.getStatus());
            view.refresh();
        });
    }

    private void broadcastGameStart() {
        GameAreaDto area  = new GameAreaDto(Utilities.GRID_COLS, Utilities.GRID_ROWS);
        CourtZoneDto court = new CourtZoneDto(
                Utilities.COURT_COL_START, Utilities.COURT_ROW_START,
                Utilities.COURT_COL_END - Utilities.COURT_COL_START + 1,
                Utilities.COURT_ROW_END  - Utilities.COURT_ROW_START + 1);
        String json = MessageParser.toJson(
                new GameStartDto(model.getSpeed(), area,
                        Protocol.COURT_SIDE_LEFT, court));
        ClientManager.broadcast(json);
    }

    private void broadcastRoleAssignments() {
        for (Player p : model.getPlayers()) {
            PositionDto pos = new PositionDto(
                    p.getLocation().getCol(), p.getLocation().getRow());
            String json = MessageParser.toJson(
                    new RoleAssignDto(p.getRole().name(), pos));
            ClientManager.sendTo(p.getStudentCode(), json);
        }
    }

    private void broadcastGameState() {
        String json = MessageParser.toJson(
                new GameStateDto(model.buildGameState()));
        ClientManager.broadcast(json);
    }

    private void checkAllDone() {
        boolean allDone = model.getPlayers().stream()
                .allMatch(p -> p.getScore() >= Utilities.MAX_SCORE);
        if (allDone && !model.getPlayers().isEmpty()) {
            model.endGame();
            ClientManager.broadcast(MessageParser.toJson(
                    new GameEndDto(Protocol.REASON_ALL_DONE)));
        }
    }
}
