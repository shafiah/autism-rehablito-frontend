
package com.autismcentre.model;

public class TherapistHomeworkStatusUpdateRequest {

    private String status;

    public TherapistHomeworkStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
