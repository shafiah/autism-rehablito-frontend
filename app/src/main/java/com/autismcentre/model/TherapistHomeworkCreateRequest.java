
package com.autismcentre.model;

public class TherapistHomeworkCreateRequest {

    private String title;
    private String instructions;
    private String dueDate;
    private Long sessionId;
    private String therapyType;

    public TherapistHomeworkCreateRequest(
            String title,
            String instructions,
            String dueDate,
            Long sessionId,
            String therapyType) {

        this.title = title;
        this.instructions = instructions;
        this.dueDate = dueDate;
        this.sessionId = sessionId;
        this.therapyType = therapyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public void setTherapyType(String therapyType) {
        this.therapyType = therapyType;
    }
}
