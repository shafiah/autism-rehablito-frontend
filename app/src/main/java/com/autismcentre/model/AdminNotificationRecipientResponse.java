
package com.autismcentre.model;

public class AdminNotificationRecipientResponse {

    private Long notificationId;
    private Long userId;
    private String recipientName;
    private String role;
    private Long branchId;
    private String branchName;
    private Boolean isRead;
    private String createdAt;
    private String readAt;

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRole() {
        return role;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
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
