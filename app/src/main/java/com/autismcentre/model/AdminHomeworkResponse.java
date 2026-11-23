package com.autismcentre.model;

public class AdminHomeworkResponse {

    private Long homeworkId;
    private Long childId;
    private String childName;
    private String parentName;

    private Long therapistId;
    private String therapistName;

    private Long branchId;
    private String branchName;

    private Long sessionId;
    private String therapyType;

    private String title;
    private String instructions;

    private String assignedDate;
    private String dueDate;
    private String status;

    private String parentRemarks;
    private String therapistRemarks;
    private String parentInstructions;

    public AdminHomeworkResponse() {
    }

    public Long getHomeworkId() {
        return homeworkId;
    }

    public void setHomeworkId(Long homeworkId) {
        this.homeworkId = homeworkId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(Long therapistId) {
        this.therapistId = therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public void setTherapistName(String therapistName) {
        this.therapistName = therapistName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public void setTherapyType(String therapyType) {
        this.therapyType = therapyType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(String assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParentRemarks() {
        return parentRemarks;
    }

    public void setParentRemarks(String parentRemarks) {
        this.parentRemarks = parentRemarks;
    }

    public String getTherapistRemarks() {
        return therapistRemarks;
    }

    public void setTherapistRemarks(String therapistRemarks) {
        this.therapistRemarks = therapistRemarks;
    }

    public String getParentInstructions() {
        return parentInstructions;
    }

    public void setParentInstructions(String parentInstructions) {
        this.parentInstructions = parentInstructions;
    }
}
