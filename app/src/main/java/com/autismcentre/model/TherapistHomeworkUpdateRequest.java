
package com.autismcentre.model;

public class TherapistHomeworkUpdateRequest {

    private String title;
    private String instructions;
    private String dueDate;

    public TherapistHomeworkUpdateRequest() {
    }

    public TherapistHomeworkUpdateRequest(
            String title,
            String instructions,
            String dueDate) {
        this.title = title;
        this.instructions = instructions;
        this.dueDate = dueDate;
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
}
