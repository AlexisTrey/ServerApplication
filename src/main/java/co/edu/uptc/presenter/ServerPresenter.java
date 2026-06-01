package co.edu.uptc.presenter;

import co.edu.uptc.dto.*;
import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.model.MoveResult;
import co.edu.uptc.network.protocol.MessageParser;
import co.edu.uptc.network.server.ClientHandler;
import co.edu.uptc.network.server.ClientManager;
import co.edu.uptc.pojo.GameOverReason;
import co.edu.uptc.pojo.Movement;
import co.edu.uptc.pojo.GameStatus;
import co.edu.uptc.pojo.Player;
import co.edu.uptc.util.Utilities;

import javax.swing.*;
import java.util.List;

public class ServerPresenter implements PresenterInterface {
    private ModelInterface model;
    private ViewInterface view;

    @Override
    public void setModel(ModelInterface model) {
        this.model = model;
    }

    @Override
    public void setView(ViewInterface view) {
        this.view = view;
    }

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
        ClientManager.broadcast(MessageParser.toJson(
                new GameEndDto(GameOverReason.SERVER_DECISION.name())));
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
                    new ConnectAckDto(false, "Partida en curso",
                            model.getStatus().name())));
            return;
        }
        if (model.findPlayer(studentCode) != null) {
            handler.sendMessage(MessageParser.toJson(
                    new ConnectAckDto(false, "Código ya registrado")));
            return;
        }
        int shortId = ClientManager.getCount();
        model.addPlayer(new Player(studentCode, shortId));
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
    public void onPlayerMove(String studentCode, String movement) {
        if (model.getStatus() != GameStatus.IN_GAME)
            return;

        Movement mov = Movement.valueOf(movement);
        MoveResult result = model.processMove(studentCode, mov);

        if (result.isBlock()) {
            // 1. BLOCK → a todos
            ClientManager.broadcast(MessageParser.toJson(
                    new BlockDto(result.getDefenderCode(),
                            result.getAttackerCode())));

            // 2. SCORE_UPDATE → al defensor
            Player defender = model.findPlayer(result.getDefenderCode());
            if (defender != null) {
                ClientManager.sendTo(defender.getStudentCode(),
                        MessageParser.toJson(new ScoreUpdateDto(
                                defender.getStudentCode(),
                                defender.getScore(),
                                defender.getRole().name())));

                // 3. ROLE_CHANGE → a todos (si hubo cambio)
                if (result.isRoleChanged()) {
                    PositionDto pos = new PositionDto(
                            defender.getLocation().getCol(),
                            defender.getLocation().getRow());
                    ClientManager.broadcast(MessageParser.toJson(
                            new RoleChangeDto(defender.getStudentCode(),
                                    defender.getRole().name(), pos)));
                }
            }
        }

        if (result.isGoal()) {
            // 1. SCORE_UPDATE → al atacante
            Player attacker = model.findPlayer(result.getAttackerCode());
            if (attacker != null) {
                ClientManager.sendTo(attacker.getStudentCode(),
                        MessageParser.toJson(new ScoreUpdateDto(
                                attacker.getStudentCode(),
                                attacker.getScore(),
                                attacker.getRole().name())));

                // 2. ROLE_CHANGE → a todos (si hubo cambio)
                if (result.isRoleChanged()) {
                    PositionDto pos = new PositionDto(
                            attacker.getLocation().getCol(),
                            attacker.getLocation().getRow());
                    ClientManager.broadcast(MessageParser.toJson(
                            new RoleChangeDto(attacker.getStudentCode(),
                                    attacker.getRole().name(), pos)));
                }
            }
        }

        // PLAYER_DONE si algún jugador llegó a 10 puntos
        List<String> finishedPlayers = model.getPlayers().stream()
                .filter(p -> p.getScore() >= Utilities.MAX_SCORE)
                .map(Player::getStudentCode)
                .toList();

        for (String code : finishedPlayers) {
            ClientManager.broadcast(MessageParser.toJson(
                    new PlayerDoneDto(code)));

            model.removePlayer(code);
        }

        // GAME_STATE siempre al final
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
        GameAreaDto area = new GameAreaDto(
                Utilities.GRID_COLS,
                Utilities.GRID_ROWS,
                Utilities.COURT_ROW_END - Utilities.COURT_ROW_START + 1);
        ClientManager.broadcast(MessageParser.toJson(
                new GameStartDto(model.getSpeed(), area)));
    }

    private void broadcastRoleAssignments() {
        for (Player p : model.getPlayers()) {
            PositionDto pos = new PositionDto(
                    p.getLocation().getCol(),
                    p.getLocation().getRow());
            ClientManager.sendTo(p.getStudentCode(),
                    MessageParser.toJson(
                            new RoleAssignDto(p.getRole().name(), pos)));
        }
    }

    private void broadcastGameState() {
        ClientManager.broadcast(MessageParser.toJson(
                new GameStateDto(model.buildGameState())));
    }

    private void checkAllDone() {
        if (model.getPlayers().isEmpty())
            return;
        boolean allDone = model.getPlayers().stream()
                .allMatch(p -> p.getScore() >= Utilities.MAX_SCORE);
        if (allDone) {
            model.endGame();
            ClientManager.broadcast(MessageParser.toJson(
                    new GameEndDto(GameOverReason.ALL_DONE.name())));
            refreshView();
        }
    }
}
