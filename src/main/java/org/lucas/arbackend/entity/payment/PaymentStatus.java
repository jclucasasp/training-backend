package org.lucas.arbackend.entity.payment;

public enum PaymentStatus {
    COMPLETE, CANCELLED, FAILED, PENDING;

    public static PaymentStatus fromString(PaymentStatus status) {
        return status;
    }
}
