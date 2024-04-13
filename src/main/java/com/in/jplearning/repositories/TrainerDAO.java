package com.in.jplearning.repositories;

import com.in.jplearning.model.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrainerDAO extends JpaRepository<Trainer,Long> {

    @Query("select t from Trainer t where t.user.userID =?1 and t.isVerify = true")
    Trainer getByUserId(Long userID);

    @Transactional
    @Modifying
    @Query(value = "Update Trainer t set t.isVerify = ?1 where t.trainerID =?2")
    Integer updateStatus(Boolean isVerify, Long trainerID);

    @Query("select t from Trainer t where t.user.role = 'USER'")
    Page<Trainer> getAllTrainer(Pageable pageable);
}
