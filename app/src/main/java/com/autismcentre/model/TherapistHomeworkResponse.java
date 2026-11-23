
package com.autismcentre.model;

public class TherapistHomeworkResponse {

    private Long homeworkId;

    private Long childId;
    private String childName;

    private Long therapistId;
    private String therapistName;

    private Long sessionId;

    private String title;
    private String instructions;

    private String assignedDate;
    private String dueDate;
    private String status;
    private String parentRemarks;
    private String therapistRemarks;
    private String parentName;
    private String createdAt;
    private String updatedAt;

    public Long getHomeworkId() {
        return homeworkId;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getAssignedDate() {
        return assignedDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public String getParentRemarks() {
        return parentRemarks;
    }

    public String getTherapistRemarks() {
        return therapistRemarks;
    }

    public void setTherapistRemarks(String therapistRemarks) {
        this.therapistRemarks = therapistRemarks;
    }
    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
