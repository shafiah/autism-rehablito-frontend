package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class AdminChildUpdateRequest {

    @SerializedName("childName")
    private String childName;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("gender")
    private String gender;

    @SerializedName("parentId")
    private Long parentId;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("medicalNotes")
    private String medicalNotes;

    public AdminChildUpdateRequest(
            String childName,
            String dateOfBirth,
            String gender,
            Long parentId,
            Long branchId,
            String medicalNotes) {

        this.childName = childName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.parentId = parentId;
        this.branchId = branchId;
        this.medicalNotes = medicalNotes;
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

    public Long getBranchId() {
        return branchId;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }
}