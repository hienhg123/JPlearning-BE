package com.in.jplearning.repositories;

import com.in.jplearning.enums.QuestionType;
import com.in.jplearning.model.User;
import com.in.jplearning.model.User_Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserExerciseDAO extends JpaRepository<User_Exercise,Long> {

    @Query("select ue from User_Exercise ue where ue.user.userID =?1 and ue.exercises.exercisesID =?2")
    List<User_Exercise> getByUser(Long userID, Long exerciseID);


    @Query("SELECT ue FROM User_Exercise ue JOIN FETCH ue.exercises e WHERE ue.user.userID = ?1 AND e.exerciseType = 'JLPT_TEST'")
    List<User_Exercise> getUserExerciseInfo(Long userID);

    @Query("select ue from User_Exercise ue where ue.user = ?1")
    List<User_Exercise> getJLPTHistoryByUser(User user);

    @Query("select ue from User_Exercise ue where ue.user =?1 and ue.exercises.exercisesID=?2 and ue.questionType =?3")
    List<User_Exercise> getJLPTByUser(User user, Long exerciseID, QuestionType questionType);

}
