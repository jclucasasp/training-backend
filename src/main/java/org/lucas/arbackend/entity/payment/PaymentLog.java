package org.lucas.arbackend.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.util.encrypt.TokenEncryptionConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_logs")
@SQLDelete(sql = "UPDATE payment_logs SET ended_at = CURRENT_TIMESTAMP AND pal_subscription = 0 WHERE pal_org_id = ?")
@SQLRestriction("ended_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pal_id")
    private Long id;

    @Column(name = "pal_pf_payment_id", unique = true, nullable = false)
    private String pfPaymentId; // PayFast unique transaction ID

    @Column(name = "pal_org_id", nullable = false)
    private Long orgId;

    @Column(name = "pal_amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "pal_sub_cycles")
    private Integer subscriptionCycles = 1;

    @Column(name = "pal_billing_date")
    private LocalDateTime billingDate;

    @Column(name = "pal_token")
    @Convert(converter = TokenEncryptionConverter.class)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "pal_payment_status", nullable = false, columnDefinition = "ENUM('COMPLETED', 'FAILED', 'PENDING')")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pal_plan_term")
    private PlanTypes planTerm;

    @Enumerated(EnumType.STRING)
    @Column(name = "pal_sub_status", columnDefinition = "ENUM('ACTIVE', 'CANCELLED', 'SUSPENDED', 'DELETED')")
    private SubscriptionStatus subscriptionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "pal_failure_code", columnDefinition = "ENUM('PRICE_MISMATCH', 'AMOUNT_MISMATCH', 'REFUND', 'PLAN_MISMATCH', 'SIGNATURE_MISMATCH', 'INSUFFICIENT_FUNDS','ORG_NOT_FOUND')")
    private FailureCode failureCode;

    @Column(name = "pal_failure_details")
    private String failureDetails;

}
