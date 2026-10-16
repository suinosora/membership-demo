package com.example.membership.repository;

import com.example.membership.entity.MembershipRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRecordRepository extends JpaRepository<MembershipRecord, Long> {

    Optional<MembershipRecord> findByOriginalId(String originalId);

    boolean existsByOriginalId(String originalId);
}
