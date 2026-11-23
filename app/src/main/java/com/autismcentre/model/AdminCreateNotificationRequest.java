
package com.autismcentre.model;

public class AdminCreateNotificationRequest {

    private String title;
    private String message;
    private String type;
    private String recipient;
    private Long branchId;

    public AdminCreateNotificationRequest(
            String title,
            String message,
            String type,
            String recipient,
            Long branchId
    ) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.recipient = recipient;
        this.branchId = branchId;
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
}
