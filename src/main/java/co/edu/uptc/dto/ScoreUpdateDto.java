package co.edu.uptc.dto;

public class ScoreUpdateDto {
    private String type;
    private String studentCode;
    private int score;
    private String role;

    public ScoreUpdateDto() {
    }

    public ScoreUpdateDto(String studentCode, int score, String role) {
        this.type = "SCORE_UPDATE";
        this.studentCode = studentCode;
        this.score = score;
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
