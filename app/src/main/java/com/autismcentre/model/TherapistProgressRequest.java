
package com.autismcentre.model;

public class TherapistProgressRequest {

    private String observations;
    private String progress;
    private String goals;
    private String activities;
    private String remarks;

    public TherapistProgressRequest(
            String observations,
            String progress,
            String goals,
            String activities,
            String remarks
    ) {
        this.observations = observations;
        this.progress = progress;
        this.goals = goals;
        this.activities = activities;
        this.remarks = remarks;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
