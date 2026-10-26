package com.quickcommerce.model;

import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.enums.PaymentStatus;
import java.time.Instant;
import java.util.Objects;

public class Payment {
    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final PaymentMethod paymentMethod;
    private volatile PaymentStatus status;
    private volatile String transactionId;
    private final Instant createdAt;
    private volatile Instant completedAt;

    public Payment(String paymentId, String orderId, double amount, PaymentMethod paymentMethod) {
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.amount = amount;
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod cannot be null");
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void markCompleted(String transactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.completedAt = Instant.now();
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void initiateRefund() {
        if (this.status == PaymentStatus.COMPLETED) {
            this.status = PaymentStatus.REFUNDED;
        }
    }
}
