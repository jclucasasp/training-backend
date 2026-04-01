package org.lucas.arbackend.entity.payment;

public enum FailureCode {
    PRICE_MISMATCH,
    AMOUNT_MISMATCH,
    REFUND,
    PLAN_MISMATCH,
    SIGNATURE_MISMATCH,
    INSUFFICIENT_FUNDS,
    DUPLICATE_PAYMENT,
    UNAUTHORISED,
    ORG_NOT_FOUND;

    public static FailureCode fromString(FailureCode failureCode) {
        return failureCode;
    }
}
