package com.in.jplearning.repositories;

import com.in.jplearning.model.User_Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserExerciseDAO extends JpaRepository<User_Exercise,Long> {

    @Query("select ue from User_Exercise ue where ue.user.userID =?1 and ue.exercises.exercisesID =?2")
    List<User_Exercise> getByUser(Long userID, Long exerciseID);
}
