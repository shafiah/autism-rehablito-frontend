
package com.autismcentre.model;

public class TherapistNoteResponse {

    private Long noteId;
    private Long childId;
    private String childName;
    private Long therapistId;
    private String therapistName;
    private Long sessionId;
    private String note;
    private String createdAt;

    public Long getNoteId() {
        return noteId;
    }

    public Long getChildId() {
        return childId;
    }

    public String getChildName() {
        return childName;
    }

    public Long getTherapistId() {
        return therapistId;
    }

    public String getTherapistName() {
        return therapistName;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
