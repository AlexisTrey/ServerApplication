package co.edu.uptc.dto;

public class PlayerDoneDto {
    private String type;
    private String studentCode;

    public PlayerDoneDto() {}
    public PlayerDoneDto(String studentCode) {
        this.type        = "PLAYER_DONE";
        this.studentCode = studentCode;
    }

    public String getType()         { return type; }
    public void setType(String type) { this.type = type; }
    public String getStudentCode()  { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
}
