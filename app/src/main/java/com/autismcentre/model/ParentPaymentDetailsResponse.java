
package com.autismcentre.model;

public class ParentPaymentDetailsResponse {

    private Long paymentId;
    private String childName;
    private String therapyType;
    private Double amount;
    private String status;
    private String paymentDate;
    private String receiptReference;

    public Long getPaymentId() {
        return paymentId;
    }

    public String getChildName() {
        return childName;
    }

    public String getTherapyType() {
        return therapyType;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public String getReceiptReference() {
        return receiptReference;
    }
}
