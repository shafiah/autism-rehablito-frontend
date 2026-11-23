
package com.autismcentre.model;

public class AdminScheduleNotificationRequest
        extends AdminCreateNotificationRequest {

    private String scheduledAt;

    public AdminScheduleNotificationRequest(
            String title,
            String message,
            String type,
            String recipient,
            Long branchId,
            String scheduledAt
    ) {

        super(
                title,
                message,
                type,
                recipient,
                branchId
        );

        this.scheduledAt = scheduledAt;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }
}
