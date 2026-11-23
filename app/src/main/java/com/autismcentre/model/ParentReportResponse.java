
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class ParentReportResponse {

    @SerializedName("reportId")
    private Long reportId;

    @SerializedName("childId")
    private Long childId;

    @SerializedName("childName")
    private String childName;

    @SerializedName("therapistId")
    private Long therapistId;

    @SerializedName("therapistName")
    private String therapistName;

    @SerializedName("reportTitle")
    private String reportTitle;

    @SerializedName("reportContent")
    private String reportContent;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("submittedAt")
    private String submittedAt;

    @SerializedName("reviewedAt")
    private String reviewedAt;

    @SerializedName("publishedAt")
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

    public String getCreatedAt() {
        return createdAt;
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
