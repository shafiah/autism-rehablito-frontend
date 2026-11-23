
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class AdminTherapistCreateRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("mobileNumber")
    private String mobileNumber;

    @SerializedName("password")
    private String password;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("therapyType")
    private String therapyType;

    @SerializedName("availability")
    private String availability;

    public AdminTherapistCreateRequest(
            String name,
            String email,
            String mobileNumber,
            String password,
            Long branchId,
            String therapyType,
            String availability) {

        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.branchId = branchId;
        this.therapyType = therapyType;
        this.availability = availability;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public String getAvailability() {
        return availability;
    }
}
