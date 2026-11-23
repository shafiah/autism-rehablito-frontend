
package com.autismcentre.model;

public class ParentChildTherapistResponse {

    private Long assignmentId;
    private Long therapistId;
    private String therapistName;
    private String therapyType;
    private Long branchId;
    private String branchName;
    private Boolean active;
    private String assignedAt;

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public Boolean getActive() {
        return active;
    }

    public String getAssignedAt() {
        return assignedAt;
    }
}
