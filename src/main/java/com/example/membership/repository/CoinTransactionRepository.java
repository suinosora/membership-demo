package com.example.membership.repository;

import com.example.membership.entity.CoinTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {

    Optional<CoinTransaction> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
