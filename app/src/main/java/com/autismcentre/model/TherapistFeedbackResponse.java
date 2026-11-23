
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class TherapistFeedbackResponse {

    @SerializedName("feedbackId")
    private Long feedbackId;

    private String childName;
    private String parentName;
    private Integer rating;
    private String message;
    private String status;
    private String createdAt;
    private String publishedAt;

    public Long getFeedbackId() {
        return feedbackId;
    }

    public String getChildName() {
        return childName;
    }

    public String getParentName() {
        return parentName;
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
