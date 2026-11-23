package com.autismcentre.model;

public class ParentCreatePaymentResponse {
    private Long paymentId;
    private Long childId;
    private String childName;
    private String therapyType;
    private Double actualFee;
    private Double discount;
    private Double finalPayable;
    private Double paidAmount;
    private String currency;
    private String razorpayKeyId;
    private String razorpayOrderId;
    private String paymentStatus;
    private String receiptReference;

    public Long getPaymentId() { return paymentId; }
    public Long getChildId() { return childId; }
    public String getChildName() { return childName; }
    public String getTherapyType() { return therapyType; }
    public Double getActualFee() { return actualFee; }
    public Double getDiscount() { return discount; }
    public Double getFinalPayable() { return finalPayable; }
    public Double getPaidAmount() { return paidAmount; }
    public String getCurrency() { return currency; }
    public String getRazorpayKeyId() { return razorpayKeyId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getReceiptReference() { return receiptReference; }
}