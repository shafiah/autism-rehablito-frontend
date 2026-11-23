
package com.autismcentre.model;

public class ParentResetPasswordRequest {

    private String resetToken;
    private String newPassword;

    public ParentResetPasswordRequest(
            String resetToken,
            String newPassword) {

        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public String getResetToken() {
        return resetToken;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
