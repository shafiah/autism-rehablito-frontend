package com.autismcentre.model;

public class ParentVerifyPaymentRequest {
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;

    public ParentVerifyPaymentRequest(
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature) {
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpaySignature = razorpaySignature;
    }
}