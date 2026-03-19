package org.lucas.arbackend.repository.payment;

import org.lucas.arbackend.entity.payment.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    boolean existByPdPaymentId(String pfPaymentId);

    List<PaymentLog> findByOrgIdOrderByCreatedAtDesc(Long orgId);
}
