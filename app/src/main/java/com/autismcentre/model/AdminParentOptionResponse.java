
package com.autismcentre.model;

public class AdminParentOptionResponse {

    private Long parentId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
    private Long branchId;
    private String branchName;
    private Boolean childLinked;

    public Long getParentId() {
        return parentId;
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

    public Boolean getActive() {
        return active;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public Boolean getChildLinked() {
        return childLinked;
    }

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
