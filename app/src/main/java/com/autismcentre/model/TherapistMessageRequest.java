
package com.autismcentre.model;

public class TherapistMessageRequest {

    private String message;

    public TherapistMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
