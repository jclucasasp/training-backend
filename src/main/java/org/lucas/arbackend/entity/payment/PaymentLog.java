package org.lucas.arbackend.entity.payment;

import jakarta.persistence.*;
import lombok.*;
import org.lucas.arbackend.entity.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pal_id")
    private Long id;

    @Column(name = "pal_pf_payment_id", unique = true, nullable = false)
    private String pfPaymentId; // Payfast unique transaction ID

    @Column(name = "pal_org_id", nullable = false)
    private Long orgId;

    @Column(name = "pal_amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "pal_payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "pal_raw_ipn_data", columnDefinition = "TEXT")
    private String rawData;
}
