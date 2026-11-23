
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class TherapistLoginResponse {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("therapistId")
    private Long therapistId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("therapyType")
    private String therapyType;

    @SerializedName("token")
    private String token;

    public Long getUserId() {
        return userId;
    }

    public Long getTherapistId() {
        return therapistId;
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

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public String getToken() {
        return token;
    }
}
