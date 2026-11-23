
package com.autismcentre.model;

public class AdminNotificationCampaignResponse {

    private Long campaignId;
    private String title;
    private String message;
    private String type;
    private String recipient;
    private Long branchId;
    private Integer totalRecipients;
    private Integer readCount;
    private Integer unreadCount;
    private String createdAt;
    private String status;
    private String scheduledAt;

    public Long getCampaignId() {
        return campaignId;
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

    public String getRecipient() {
        return recipient;
    }

    public Long getBranchId() {
        return branchId;
    }

    public Integer getTotalRecipients() {
        return totalRecipients;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }
}
