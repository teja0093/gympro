package com.gympro.payment_service.repository;

import com.gympro.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String orderId);
    List<Payment> findByMemberEmailOrderByCreatedAtDesc(String email);
}