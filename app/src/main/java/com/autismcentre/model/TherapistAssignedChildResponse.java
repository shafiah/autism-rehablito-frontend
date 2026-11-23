package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class TherapistAssignedChildResponse {

    @SerializedName("assignmentId")
    private Long assignmentId;

    @SerializedName("childId")
    private Long childId;

    @SerializedName("childName")
    private String childName;

    @SerializedName("therapistId")
    private Long therapistId;

    @SerializedName("therapistName")
    private String therapistName;

    @SerializedName("therapistEmail")
    private String therapistEmail;

    @SerializedName("therapyType")
    private String therapyType;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("assignedAt")
    private String assignedAt;

    @SerializedName("unassignedAt")
    private String unassignedAt;

    public Long getAssignmentId() {
        return assignmentId;
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

    public String getTherapistEmail() {
        return therapistEmail;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public Boolean getActive() {
        return active;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public String getUnassignedAt() {
        return unassignedAt;
    }
}