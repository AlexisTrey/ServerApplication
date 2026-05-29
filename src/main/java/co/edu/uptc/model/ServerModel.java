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
    public MoveResult processMove(String studentCode, Direction direction) {
        Player player = findPlayer(studentCode);
        if (player == null) return MoveResult.rejected();

        Location target = computeTarget(player.getLocation(), direction);

        if (!isInsideBounds(target))               return MoveResult.rejected();
        if (isProtectedZoneViolation(player, target)) return MoveResult.rejected();

        Player occupant = findPlayerAtLocation(target);
        if (occupant != null) {
            return handleCollision(player, occupant);
        }

        player.setLocation(target);
        return checkArrival(player);
    }

    @Override
    public void setSpeed(int speedMs) { this.speedMs = speedMs; }

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
                        p.getStudentCode(),
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
                players.get(i).setProgressCount(0);
                players.get(i).setLocation(findFreeSpawn(role));
            }
        }
    }

    private Location computeTarget(Location loc, Direction dir) {
        return switch (dir) {
            case UP    -> new Location(loc.getCol(),     loc.getRow() - 1);
            case DOWN  -> new Location(loc.getCol(),     loc.getRow() + 1);
            case LEFT  -> new Location(loc.getCol() - 1, loc.getRow());
            case RIGHT -> new Location(loc.getCol() + 1, loc.getRow());
        };
    }

    private boolean isInsideBounds(Location loc) {
        return loc.getCol() >= 0 && loc.getCol() < Utilities.GRID_COLS
                && loc.getRow() >= 0 && loc.getRow() < Utilities.GRID_ROWS;
    }

    private boolean isProtectedZoneViolation(Player player, Location target) {
        if (player.getRole() == Role.DEFENDER && isAttackerSpawn(target))  return true;
        if (player.getRole() == Role.ATTACKER && isAboveBelowCourt(target)) return true;
        return false;
    }

    private boolean isAttackerSpawn(Location loc) {
        return loc.getCol() >= Utilities.ATK_SPAWN_COL_START
                && loc.getCol() <= Utilities.ATK_SPAWN_COL_END;
    }

    private boolean isAboveBelowCourt(Location loc) {
        boolean inCourtCols    = loc.getCol() >= Utilities.COURT_COL_START
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

    private MoveResult handleCollision(Player mover, Player occupant) {
        if (mover.getRole() == Role.ATTACKER && occupant.getRole() == Role.DEFENDER) {
            returnToSpawn(mover);
            occupant.setScore(occupant.getScore() + 1);
            occupant.setProgressCount(occupant.getProgressCount() + 1);
            boolean roleChanged = checkRoleChange(occupant);
            return MoveResult.block(mover.getStudentCode(),
                    occupant.getStudentCode(),
                    roleChanged);
        }
        return MoveResult.rejected();
    }

    private MoveResult checkArrival(Player player) {
        if (player.getRole() != Role.ATTACKER)       return MoveResult.moved();
        if (!isInsideCourt(player.getLocation()))    return MoveResult.moved();

        player.setScore(player.getScore() + 1);
        player.setProgressCount(player.getProgressCount() + 1);
        returnToSpawn(player);
        boolean roleChanged = checkRoleChange(player);
        return MoveResult.goal(player.getStudentCode(), roleChanged);
    }

    private boolean isInsideCourt(Location loc) {
        return loc.getCol() >= Utilities.COURT_COL_START
                && loc.getCol() <= Utilities.COURT_COL_END
                && loc.getRow() >= Utilities.COURT_ROW_START
                && loc.getRow() <= Utilities.COURT_ROW_END;
    }

    private void returnToSpawn(Player player) {
        player.setLocation(findFreeSpawn(player.getRole()));
    }

    private Location findFreeSpawn(Role role) {
        int col = (role == Role.ATTACKER)
                ? Utilities.ATK_SPAWN_COL_START
                : Utilities.DEF_SPAWN_COL;
        for (int row = 0; row < Utilities.GRID_ROWS; row++) {
            Location candidate = new Location(col, row);
            if (findPlayerAtLocation(candidate) == null) return candidate;
        }
        return new Location(col, 0);
    }

    private boolean checkRoleChange(Player player) {
        if (player.getProgressCount() < Utilities.ROLE_CHANGE_COUNT) return false;
        Role newRole = (player.getRole() == Role.ATTACKER)
                ? Role.DEFENDER : Role.ATTACKER;
        player.setRole(newRole);
        player.setProgressCount(0);
        player.setLocation(findFreeSpawn(newRole));
        return true;
    }
}
