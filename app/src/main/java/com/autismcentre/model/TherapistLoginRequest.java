
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class TherapistLoginRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public TherapistLoginRequest(String email, String password) {
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
