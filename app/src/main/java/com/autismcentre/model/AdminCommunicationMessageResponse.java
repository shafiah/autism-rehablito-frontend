
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class AdminCommunicationMessageResponse {

    @SerializedName("messageId")
    private Long messageId;

    @SerializedName("childId")
    private Long childId;

    @SerializedName("childName")
    private String childName;

    @SerializedName("senderUserId")
    private Long senderUserId;

    @SerializedName("senderUserName")
    private String senderUserName;

    @SerializedName("receiverUserId")
    private Long receiverUserId;

    @SerializedName("receiverUserName")
    private String receiverUserName;

    @SerializedName("senderRole")
    private String senderRole;

    @SerializedName("receiverRole")
    private String receiverRole;

    @SerializedName("message")
    private String message;

    @SerializedName("read")
    private Boolean read;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public Long getMessageId() { return messageId; }
    public Long getChildId() { return childId; }
    public String getChildName() { return childName; }
    public Long getSenderUserId() { return senderUserId; }
    public String getSenderUserName() { return senderUserName; }
    public Long getReceiverUserId() { return receiverUserId; }
    public String getReceiverUserName() { return receiverUserName; }
    public String getSenderRole() { return senderRole; }
    public String getReceiverRole() { return receiverRole; }
    public String getMessage() { return message; }
    public Boolean getRead() { return read; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
