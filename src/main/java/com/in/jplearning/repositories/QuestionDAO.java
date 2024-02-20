package com.in.jplearning.repositories;


import com.in.jplearning.dtos.QuestionDTO;
import com.in.jplearning.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionDAO extends JpaRepository<Question,Long> {

    @Query("select q from Question q where q.exercises.exercisesID =?1")
    List<Question> getByExerciseId(Long exerciseID);




}
