
package com.autismcentre.model;

public class ParentForgotPasswordRequest {

    private String email;

    public ParentForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
