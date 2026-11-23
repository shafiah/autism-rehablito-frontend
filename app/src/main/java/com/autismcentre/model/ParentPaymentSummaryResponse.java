package com.autismcentre.model;

public class ParentPaymentSummaryResponse {
    private Long childId;
    private String childName;
    private String therapyType;
    private Double actualFee;
    private Double discount;
    private Double finalPayable;
    private String currency;

    public Long getChildId() { return childId; }
    public String getChildName() { return childName; }
    public String getTherapyType() { return therapyType; }
    public Double getActualFee() { return actualFee; }
    public Double getDiscount() { return discount; }
    public Double getFinalPayable() { return finalPayable; }
    public String getCurrency() { return currency; }
}