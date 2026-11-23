
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminChildResponse {

    @SerializedName("childId")
    private Long childId;

    @SerializedName("childName")
    private String childName;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("gender")
    private String gender;

    @SerializedName("parentId")
    private Long parentId;

    @SerializedName("parentName")
    private String parentName;

    @SerializedName("parentEmail")
    private String parentEmail;

    @SerializedName("parentMobileNumber")
    private String parentMobileNumber;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("medicalNotes")
    private String medicalNotes;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("therapyTypes")
    private List<String> therapyTypes;

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public String getParentMobileNumber() {
        return parentMobileNumber;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public Boolean getActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<String> getTherapyTypes() {
        return therapyTypes;
    }
}
