package com.example.membership.repository;

import com.example.membership.entity.Membership;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByUserIdAndMembershipType(Long userId, String membershipType);

    /** 行级悲观锁(对应 Go 的 SELECT ... FOR UPDATE)。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Membership m where m.userId = :userId and m.membershipType = :type")
    Optional<Membership> findForUpdate(@Param("userId") Long userId, @Param("type") String type);
}
