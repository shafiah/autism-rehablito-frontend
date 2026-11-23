
package com.autismcentre.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class AdminPaymentSummaryResponse {

    @SerializedName("totalCollection")
    private BigDecimal totalCollection;

    @SerializedName("pendingTransactionAmount")
    private BigDecimal pendingTransactionAmount;

    @SerializedName("totalTransactions")
    private Long totalTransactions;

    @SerializedName("successfulTransactions")
    private Long successfulTransactions;

    @SerializedName("pendingTransactions")
    private Long pendingTransactions;

    @SerializedName("failedTransactions")
    private Long failedTransactions;

    public AdminPaymentSummaryResponse() {
    }

    public BigDecimal getTotalCollection() {
        return totalCollection;
    }

    public void setTotalCollection(BigDecimal totalCollection) {
        this.totalCollection = totalCollection;
    }

    public BigDecimal getPendingTransactionAmount() {
        return pendingTransactionAmount;
    }

    public void setPendingTransactionAmount(
            BigDecimal pendingTransactionAmount) {
        this.pendingTransactionAmount = pendingTransactionAmount;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Long getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(Long successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public Long getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(Long pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Long getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(Long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }
}
