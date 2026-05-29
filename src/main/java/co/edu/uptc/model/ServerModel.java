package co.edu.uptc.model;

import co.edu.uptc.dto.PlayerDto;
import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.pojo.*;
import co.edu.uptc.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerModel implements ModelInterface {
    private final List<Player> players =
            Collections.synchronizedList(new ArrayList<>());
    private GameStatus status  = GameStatus.WAITING;
    private int        speedMs = Utilities.DEFAULT_SPEED_MS;

    @Override
    public void addPlayer(Player player) {
        players.add(player);
    }

    @Override
    public void removePlayer(String studentCode) {
        players.removeIf(p -> studentCode.equals(p.getStudentCode()));
    }

    @Override
    public void startGame() {
        status = GameStatus.IN_PROGRESS;
        assignRolesAndPositions();
    }

    @Override
    public void endGame() {
        status = GameStatus.FINISHED;
    }

    @Override
    public boolean processMove(String studentCode, Direction direction) {
        Player player = findPlayer(studentCode);
        if (player == null) return false;
        Location target = computeTarget(player.getLocation(), direction);
        if (!isInsideBounds(target))        return false;
        if (isProtectedZoneViolation(player, target)) return false;
        Player blocker = findPlayerAtLocation(target);
        if (blocker != null) {
            return handleCollision(player, blocker);
        }
        player.setLocation(target);
        checkArrival(player);
        return true;
    }

    @Override
    public void setSpeed(int speedMs) {
        this.speedMs = speedMs;
    }

    @Override
    public int getSpeed() { return speedMs; }

    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public GameStatus getStatus() { return status; }

    @Override
    public Player findPlayer(String studentCode) {
        return players.stream()
                .filter(p -> studentCode.equals(p.getStudentCode()))
                .findFirst().orElse(null);
    }

    @Override
    public List<PlayerDto> buildGameState() {
        List<PlayerDto> state = new ArrayList<>();
        synchronized (players) {
            for (Player p : players) {
                state.add(new PlayerDto(
                        String.valueOf(p.getShortId()),
                        p.getRole().name(),
                        p.getLocation().getCol(),
                        p.getLocation().getRow()));
            }
        }
        return state;
    }

    private void assignRolesAndPositions() {
        synchronized (players) {
            for (int i = 0; i < players.size(); i++) {
                Role role = (i % 2 == 0) ? Role.ATTACKER : Role.DEFENDER;
                players.get(i).setRole(role);
                players.get(i).setLocation(buildSpawnLocation(role, i / 2));
                players.get(i).setProgressCount(0);
            }
        }
    }

    private Location buildSpawnLocation(Role role, int index) {
        if (role == Role.ATTACKER) {
            int col = Utilities.ATK_SPAWN_COL_START + (index % 2);
            int row = index / 2;
            return new Location(col, row);
        }
        return new Location(Utilities.DEF_SPAWN_COL, index);
    }

    private Location computeTarget(Location loc, Direction dir) {
        return switch (dir) {
            case UP    -> new Location(loc.getCol(), loc.getRow() - 1);
            case DOWN  -> new Location(loc.getCol(), loc.getRow() + 1);
            case LEFT  -> new Location(loc.getCol() - 1, loc.getRow());
            case RIGHT -> new Location(loc.getCol() + 1, loc.getRow());
        };
    }

    private boolean isInsideBounds(Location loc) {
        return loc.getCol() >= 0 && loc.getCol() < Utilities.GRID_COLS
                && loc.getRow() >= 0 && loc.getRow() < Utilities.GRID_ROWS;
    }

    private boolean isProtectedZoneViolation(Player player, Location target) {
        if (player.getRole() == Role.DEFENDER && isAttackerSpawn(target)) return true;
        if (player.getRole() == Role.ATTACKER && isAboveBelowCourt(target)) return true;
        return false;
    }

    private boolean isAttackerSpawn(Location loc) {
        return loc.getCol() >= Utilities.ATK_SPAWN_COL_START
                && loc.getCol() <= Utilities.ATK_SPAWN_COL_END;
    }

    private boolean isAboveBelowCourt(Location loc) {
        boolean inCourtCols = loc.getCol() >= Utilities.COURT_COL_START
                && loc.getCol() <= Utilities.COURT_COL_END;
        boolean outsideCourtRows = loc.getRow() < Utilities.COURT_ROW_START
                || loc.getRow() > Utilities.COURT_ROW_END;
        return inCourtCols && outsideCourtRows;
    }

    private Player findPlayerAtLocation(Location loc) {
        return players.stream()
                .filter(p -> loc.equals(p.getLocation()))
                .findFirst().orElse(null);
    }

    private boolean handleCollision(Player mover, Player other) {
        if (mover.getRole() == Role.ATTACKER && other.getRole() == Role.DEFENDER) {
            returnToSpawn(mover);
            other.setProgressCount(other.getProgressCount() + 1);
            checkRoleChange(other);
            return false;
        }
        return false;
    }

    private void returnToSpawn(Player player) {
        int index = players.indexOf(player);
        player.setLocation(buildSpawnLocation(player.getRole(), index / 2));
        player.setProgressCount(0);
    }

    private void checkArrival(Player player) {
        if (player.getRole() != Role.ATTACKER) return;
        if (!isInsideCourt(player.getLocation())) return;
        player.setScore(player.getScore() + 1);
        player.setProgressCount(player.getProgressCount() + 1);
        returnToSpawn(player);
        checkRoleChange(player);
    }

    private boolean isInsideCourt(Location loc) {
        return loc.getCol() >= Utilities.COURT_COL_START
                && loc.getCol() <= Utilities.COURT_COL_END
                && loc.getRow() >= Utilities.COURT_ROW_START
                && loc.getRow() <= Utilities.COURT_ROW_END;
    }

    private void checkRoleChange(Player player) {
        if (player.getProgressCount() >= Utilities.ROLE_CHANGE_COUNT) {
            Role newRole = (player.getRole() == Role.ATTACKER)
                    ? Role.DEFENDER : Role.ATTACKER;
            player.setRole(newRole);
            player.setProgressCount(0);
            int index = players.indexOf(player);
            player.setLocation(buildSpawnLocation(newRole, index / 2));
        }
    }
}
