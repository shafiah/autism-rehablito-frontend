package com.autismcentre.model;

public class AdminSessionCreateRequest {

    private Long childId;
    private Long therapistId;
    private Long branchId;

    private String sessionDate;
    private String startTime;
    private String endTime;

    private String remarks;

    public AdminSessionCreateRequest(
            Long childId,
            Long therapistId,
            Long branchId,
            String sessionDate,
            String startTime,
            String endTime,
            String remarks) {

        this.childId = childId;
        this.therapistId = therapistId;
        this.branchId = branchId;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.remarks = remarks;
    }

    public Long getChildId() {
        return childId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public Long getBranchId() {
        return branchId;
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

    public String getRemarks() {
        return remarks;
    }
}