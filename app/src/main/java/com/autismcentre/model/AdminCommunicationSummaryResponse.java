
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class AdminCommunicationSummaryResponse {

    @SerializedName("childId")
    private Long childId;

    @SerializedName("childName")
    private String childName;

    @SerializedName("parentId")
    private Long parentId;

    @SerializedName("parentUserId")
    private Long parentUserId;

    @SerializedName("parentName")
    private String parentName;

    @SerializedName("branchId")
    private Long branchId;

    @SerializedName("branchName")
    private String branchName;

    @SerializedName("therapistId")
    private Long therapistId;

    @SerializedName("therapistUserId")
    private Long therapistUserId;

    @SerializedName("therapistName")
    private String therapistName;

    @SerializedName("latestMessageId")
    private Long latestMessageId;

    @SerializedName("latestMessage")
    private String latestMessage;

    @SerializedName("latestSenderUserId")
    private Long latestSenderUserId;

    @SerializedName("latestSenderName")
    private String latestSenderName;

    @SerializedName("latestSenderRole")
    private String latestSenderRole;

    @SerializedName("latestReceiverUserId")
    private Long latestReceiverUserId;

    @SerializedName("latestReceiverName")
    private String latestReceiverName;

    @SerializedName("latestReceiverRole")
    private String latestReceiverRole;

    @SerializedName("latestMessageAt")
    private String latestMessageAt;

    @SerializedName("parentUnreadCount")
    private Long parentUnreadCount;

    @SerializedName("therapistUnreadCount")
    private Long therapistUnreadCount;

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getParentUserId() {
        return parentUserId;
    }

    public String getParentName() {
        return parentName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public Long getTherapistUserId() {
        return therapistUserId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public Long getLatestMessageId() {
        return latestMessageId;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public Long getLatestSenderUserId() {
        return latestSenderUserId;
    }

    public String getLatestSenderName() {
        return latestSenderName;
    }

    public String getLatestSenderRole() {
        return latestSenderRole;
    }

    public Long getLatestReceiverUserId() {
        return latestReceiverUserId;
    }

    public String getLatestReceiverName() {
        return latestReceiverName;
    }

    public String getLatestReceiverRole() {
        return latestReceiverRole;
    }

    public String getLatestMessageAt() {
        return latestMessageAt;
    }

    public Long getParentUnreadCount() {
        return parentUnreadCount;
    }

    public Long getTherapistUnreadCount() {
        return therapistUnreadCount;
    }
}
