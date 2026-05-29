package co.edu.uptc.dto;

public class BlockDto {
    private String type;
    private String defenderCode;
    private String attackerCode;

    public BlockDto() {}
    public BlockDto(String defenderCode, String attackerCode) {
        this.type         = "BLOCK";
        this.defenderCode = defenderCode;
        this.attackerCode = attackerCode;
    }

    public String getType()           { return type; }
    public void setType(String type)  { this.type = type; }
    public String getDefenderCode()   { return defenderCode; }
    public void setDefenderCode(String defenderCode) { this.defenderCode = defenderCode; }
    public String getAttackerCode()   { return attackerCode; }
    public void setAttackerCode(String attackerCode) { this.attackerCode = attackerCode; }
}
