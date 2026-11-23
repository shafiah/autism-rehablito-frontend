
package com.autismcentre.model;

public class ParentMessageRequest {

    private String message;

    public ParentMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
