
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

public class AdminMessageRequest {

    @SerializedName("receiverUserId")
    private Long receiverUserId;

    @SerializedName("conversationTherapistUserId")
    private Long conversationTherapistUserId;

    @SerializedName("message")
    private String message;

    public AdminMessageRequest(
            Long receiverUserId,
            Long conversationTherapistUserId,
            String message) {

        this.receiverUserId = receiverUserId;
        this.conversationTherapistUserId =
                conversationTherapistUserId;
        this.message = message;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public Long getConversationTherapistUserId() {
        return conversationTherapistUserId;
    }

    public String getMessage() {
        return message;
    }
}
