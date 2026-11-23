
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class NotificationResponse {

    @SerializedName("notificationId")
    private Long id;

    private Long branchId;
    private String title;
    private String message;
    private String type;
    private String referenceType;
    private Long referenceId;
    private Boolean isRead;
    private String createdAt;
    private String readAt;

    public Long getId() {
        return id;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getReadAt() {
        return readAt;
    }
}
