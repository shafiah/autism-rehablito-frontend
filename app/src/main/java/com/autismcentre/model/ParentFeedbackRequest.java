
package com.autismcentre.model;

public class ParentFeedbackRequest {

    private Long childId;
    private Long therapistId;
    private Integer rating;
    private String message;

    public ParentFeedbackRequest(
            Long childId,
            Long therapistId,
            Integer rating,
            String message
    ) {
        this.childId = childId;
        this.therapistId = therapistId;
        this.rating = rating;
        this.message = message;
    }

    public Long getChildId() {
        return childId;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getMessage() {
        return message;
    }
}
