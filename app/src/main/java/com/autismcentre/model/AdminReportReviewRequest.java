
package com.autismcentre.model;

public class AdminReportReviewRequest {

    private String adminRemarks;

    public AdminReportReviewRequest(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }
}
