
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class ParentHomeworkResponse {

    @SerializedName("homeworkId")
    private Long homeworkId;

    @SerializedName("childId")
    private Long childId;

    @SerializedName("childName")
    private String childName;

    @SerializedName("therapistId")
    private Long therapistId;

    @SerializedName("therapistName")
    private String therapistName;

    @SerializedName("sessionId")
    private Long sessionId;

    @SerializedName("therapyType")
    private String therapyType;

    @SerializedName("title")
    private String title;

    @SerializedName("instructions")
    private String instructions;

    @SerializedName("assignedDate")
    private String assignedDate;

    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("status")
    private String status;

    @SerializedName("parentRemarks")
    private String parentRemarks;

    @SerializedName("therapistRemarks")
    private String therapistRemarks;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
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

    public String getTherapyType() {
        return therapyType;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
