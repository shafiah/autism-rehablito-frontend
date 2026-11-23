
package com.autismcentre.model;

public class TherapistChildResponse {

    private Long childId;
    private String childName;
    private String dateOfBirth;
    private String gender;
    private Long branchId;
    private String branchName;
    private String medicalNotes;
    private Boolean active;
    private String createdAt;
    private String parentName;

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

    public String getParentName() {
        return parentName;
    }
}
