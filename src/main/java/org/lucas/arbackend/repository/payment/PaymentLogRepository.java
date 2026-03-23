package org.lucas.arbackend.repository.payment;

import org.lucas.arbackend.entity.payment.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    boolean existsByPfPaymentId(String pfPaymentId);

    List<PaymentLog> findByOrgIdOrderByCreatedAtDesc(Long orgId);
}
