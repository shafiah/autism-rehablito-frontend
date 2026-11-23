
package com.autismcentre.model;

import java.util.List;

public class AdminChildCreateRequest {

    private String childName;
    private String dateOfBirth;
    private String gender;
    private Long parentId;
    private Long branchId;
    private String medicalNotes;
    private List<String> therapyTypes;

    public AdminChildCreateRequest(
            String childName,
            String dateOfBirth,
            String gender,
            Long parentId,
            Long branchId,
            String medicalNotes,
            List<String> therapyTypes) {

        this.childName = childName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.parentId = parentId;
        this.branchId = branchId;
        this.medicalNotes = medicalNotes;
        this.therapyTypes = therapyTypes;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public List<String> getTherapyTypes() {
        return therapyTypes;
    }

    public void setTherapyTypes(List<String> therapyTypes) {
        this.therapyTypes = therapyTypes;
    }
}
