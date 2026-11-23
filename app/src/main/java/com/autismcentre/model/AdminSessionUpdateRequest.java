package com.autismcentre.model;

public class AdminSessionUpdateRequest {

    private String sessionDate;
    private String startTime;
    private String endTime;
    private String remarks;

    public AdminSessionUpdateRequest(
            String sessionDate,
            String startTime,
            String endTime,
            String remarks) {

        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.remarks = remarks;
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