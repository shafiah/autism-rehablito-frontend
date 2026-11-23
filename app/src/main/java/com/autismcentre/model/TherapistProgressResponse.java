
package com.autismcentre.model;

public class TherapistProgressResponse {

    private Long progressId;
    private Long childId;
    private String childName;
    private Long therapistId;
    private String therapistName;
    private Long sessionId;
    private String sessionDate;
    private String startTime;
    private String endTime;
    private String observations;
    private String progress;
    private String goals;
    private String activities;
    private String remarks;
    private String createdAt;
    private String updatedAt;

    public Long getProgressId() {
        return progressId;
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

    public String getSessionDate() {
        return sessionDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
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

    public String getUpdatedAt() {
        return updatedAt;
    }
}
