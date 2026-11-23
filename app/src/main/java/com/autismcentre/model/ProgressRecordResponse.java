
package com.autismcentre.model;

public class ProgressRecordResponse {

    private Long id;
    private Long childId;
    private String childName;
    private Long sessionId;
    private String sessionType;
    private String sessionDate;
    private Long therapistId;
    private String therapistName;
    private String observations;
    private String progress;
    private String goals;
    private String activities;
    private String remarks;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public String getObservations() {
        return observations;
    }

    public String getProgress() {
        return progress;
    }

    public String getGoals() {
        return goals;
    }

    public String getActivities() {
        return activities;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
