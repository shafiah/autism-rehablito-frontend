
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class ParentFeedbackResponse {

    @SerializedName("feedbackId")
    private Long feedbackId;

    private Long childId;
    private Long therapistId;
    private Integer rating;
    private String message;
    private String status;
    private String createdAt;
    private String publishedAt;

    public Long getFeedbackId() {
        return feedbackId;
    }

    public Long getChildId() {
        return childId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
