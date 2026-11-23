
package com.autismcentre.model;

public class TherapistMessageResponse {

    private Long messageId;
    private Long childId;
    private String childName;
    private Long senderUserId;
    private String senderName;
    private Long receiverUserId;
    private String receiverName;
    private String message;
    private Boolean read;
    private String createdAt;
    private String updatedAt;

    public Long getMessageId() {
        return messageId;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public String getSenderName() {
        return senderName;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
