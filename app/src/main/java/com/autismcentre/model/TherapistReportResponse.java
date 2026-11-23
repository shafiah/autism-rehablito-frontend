
package com.autismcentre.model;

public class TherapistReportResponse {

    private Long reportId;
    private Long childId;
    private String childName;

    private Long therapistId;
    private String therapistName;

    private String reportTitle;
    private String reportContent;
    private String status;
    private String adminRemarks;

    private String createdAt;
    private String updatedAt;
    private String submittedAt;
    private String reviewedAt;
    private String publishedAt;

    public Long getReportId() {
        return reportId;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public String getReportContent() {
        return reportContent;
    }

    public String getStatus() {
        return status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
