
package com.autismcentre.model;

public class AdminLoginResponse {

    private Long userId;
    private String name;
    private String email;
    private String role;
    private String token;

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
