
package com.autismcentre.model;

public class ParentSessionResponse {

    private Long sessionId;
    private Long childId;
    private String childName;

    private Long therapistId;
    private String therapistName;

    private String therapyType;

    private Long branchId;
    private String branchName;

    private String sessionDate;
    private String startTime;
    private String endTime;

    private String status;
    private String remarks;

    private String createdAt;
    private String updatedAt;

    public Long getSessionId() {
        return sessionId;
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

    public String getTherapyType() {
        return therapyType;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
