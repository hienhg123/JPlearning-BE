package com.in.jplearning.repositories;

import com.in.jplearning.model.Trainer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrainerDAO extends JpaRepository<Trainer,Long> {

    @Query("select t from Trainer t where t.user.userID =?1")
    Trainer getByUserId(Long userID);

    @Transactional
    @Modifying
    @Query(value = "Update Trainer t set t.isVerify = ?1 where t.trainerID =?2")
    Integer updateStatus(Boolean isVerify, Long trainerID);
}
