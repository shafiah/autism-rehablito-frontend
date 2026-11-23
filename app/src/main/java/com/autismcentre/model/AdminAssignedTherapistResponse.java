package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class AdminAssignedTherapistResponse {

    @SerializedName("therapistId")
    private Long therapistId;

    @SerializedName("therapistUserId")
    private Long therapistUserId;

    @SerializedName("therapistName")
    private String therapistName;

    @SerializedName("therapyType")
    private String therapyType;

    public Long getTherapistId() {
        return therapistId;
    }

    public Long getTherapistUserId() {
        return therapistUserId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public String getTherapyType() {
        return therapyType;
    }
}