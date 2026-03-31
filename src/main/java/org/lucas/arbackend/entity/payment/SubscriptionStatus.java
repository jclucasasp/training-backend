package org.lucas.arbackend.entity.payment;

public enum SubscriptionStatus {
    ACTIVE, CANCELLED, SUSPENDED, DELETED;

    private static SubscriptionStatus fromString(SubscriptionStatus subscriptionStatus) {
        return subscriptionStatus;
    }
}
