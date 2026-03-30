package org.lucas.arbackend.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.util.encrypt.TokenEncryptionConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_logs")
@SQLDelete(sql = "UPDATE payment_logs SET ended_at = CURRENT_TIMESTAMP AND pal_subscription = 0 WHERE pal_org_id = ?")
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

    @Column(name = "pal_subscription")
    private boolean subscription = false;

    @Column(name = "pal_billing_date")
    private LocalDateTime billingDate;

    @Column(name = "pal_token")
    @Convert(converter = TokenEncryptionConverter.class)
    private String token;

    @Column(name = "pal_payment_status", nullable = false)
    private String paymentStatus;

}
