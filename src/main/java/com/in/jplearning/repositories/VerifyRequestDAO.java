package com.in.jplearning.repositories;

import com.in.jplearning.model.VerifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VerifyRequestDAO extends JpaRepository<VerifyRequest,Long> {

    @Query(value = "SELECT vr FROM VerifyRequest vr WHERE vr.approved = false ORDER BY vr.requestTimestamp DESC")
    Page<VerifyRequest> getAllPendingRequest(Pageable pageable);

    Page<VerifyRequest> findByApproved(Boolean isApproved, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "Update VerifyRequest vr set vr.approved = ?1 where vr.requestID =?2")
    Integer updateStatus(boolean approved, long requestID);
}
