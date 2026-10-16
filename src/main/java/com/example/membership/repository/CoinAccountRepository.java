package com.example.membership.repository;

import com.example.membership.entity.CoinAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoinAccountRepository extends JpaRepository<CoinAccount, Long> {

    Optional<CoinAccount> findByUserId(Long userId);

    /** 行级悲观锁。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CoinAccount c where c.userId = :userId")
    Optional<CoinAccount> findForUpdate(@Param("userId") Long userId);
}
