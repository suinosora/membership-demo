package com.example.membership.repository;

import com.example.membership.entity.Prop;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropRepository extends JpaRepository<Prop, Long> {

    Optional<Prop> findByUserIdAndPropType(Long userId, String propType);

    /** 行级悲观锁,并只锁「仍有剩余」的道具(对应 Go 的 WHERE total > used FOR UPDATE)。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prop p where p.userId = :userId and p.propType = :type and p.total > p.used")
    Optional<Prop> findAvailableForUpdate(@Param("userId") Long userId, @Param("type") String type);
}
