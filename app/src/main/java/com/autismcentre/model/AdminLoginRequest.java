
package com.autismcentre.model;

public class AdminLoginRequest {

    private String email;
    private String password;

    public AdminLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
