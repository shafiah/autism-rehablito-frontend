
package com.autismcentre.model;

public class ParentVerifyOtpRequest {

    private String email;
    private String otp;

    public ParentVerifyOtpRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public String getOtp() {
        return otp;
    }
}
