package com.in.jplearning.repositories;

import com.in.jplearning.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainerDAO extends JpaRepository<Trainer,Long> {

    @Query("select t from Trainer t where t.user.userID =?1")
    Trainer getByUserId(Long userID);
}
