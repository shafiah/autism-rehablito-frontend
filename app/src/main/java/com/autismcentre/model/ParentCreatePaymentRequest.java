package com.autismcentre.model;

public class ParentCreatePaymentRequest {
    private Long childId;
    private String therapyType;

    public ParentCreatePaymentRequest(Long childId, String therapyType) {
        this.childId = childId;
        this.therapyType = therapyType;
    }
}
