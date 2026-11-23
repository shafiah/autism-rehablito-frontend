
package com.autismcentre.model;

public class AdminBranchResponse {

    private Long id;
    private String branchName;
    private String address;
    private Boolean active;

    public Long getId() {
        return id;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

    public Boolean getActive() {
        return active;
    }
}
