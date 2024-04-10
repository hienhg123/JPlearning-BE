package com.in.jplearning.repositories;

import com.in.jplearning.enums.Status;
import com.in.jplearning.model.VerifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface VerifyRequestDAO extends JpaRepository<VerifyRequest,Long> {

    @Query(value = "SELECT vr FROM VerifyRequest vr WHERE vr.status = 'PENDING'")
    Page<VerifyRequest> getAllPendingRequest(Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "Update VerifyRequest vr set vr.status = ?1 where vr.requestID =?2")
    Integer updateStatus(Status status, long requestID);

    @Query(value = "select vr from VerifyRequest vr where vr.trainer.user.userID =?1 and vr.status ='PENDING'")
    List<VerifyRequest> getByUserId(Long userID);
}
