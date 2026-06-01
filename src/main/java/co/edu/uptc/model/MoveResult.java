package co.edu.uptc.model;

public class MoveResult {
    public enum Type {
        MOVED, REJECTED, BLOCK, GOAL
    }

    private final Type type;
    private final String attackerCode;
    private final String defenderCode;
    private final boolean roleChanged;

    private MoveResult(Type type, String attackerCode,
            String defenderCode, boolean roleChanged) {
        this.type = type;
        this.attackerCode = attackerCode;
        this.defenderCode = defenderCode;
        this.roleChanged = roleChanged;
    }

    public static MoveResult moved() {
        return new MoveResult(Type.MOVED, null, null, false);
    }

    public static MoveResult rejected() {
        return new MoveResult(Type.REJECTED, null, null, false);
    }

    public static MoveResult block(String attackerCode, String defenderCode,
            boolean defenderRoleChanged) {
        return new MoveResult(Type.BLOCK, attackerCode, defenderCode, defenderRoleChanged);
    }

    public static MoveResult goal(String attackerCode, boolean attackerRoleChanged) {
        return new MoveResult(Type.GOAL, attackerCode, null, attackerRoleChanged);
    }

    public boolean isBlock() {
        return type == Type.BLOCK;
    }

    public boolean isGoal() {
        return type == Type.GOAL;
    }

    public boolean isMoved() {
        return type == Type.MOVED;
    }

    public boolean isRejected() {
        return type == Type.REJECTED;
    }

    public boolean isRoleChanged() {
        return roleChanged;
    }

    public String getAttackerCode() {
        return attackerCode;
    }

    public String getDefenderCode() {
        return defenderCode;
    }

    public Type getType() {
        return type;
    }
}
