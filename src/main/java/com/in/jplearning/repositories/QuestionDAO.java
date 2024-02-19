package com.in.jplearning.repositories;


import com.in.jplearning.dtos.QuestionDTO;
import com.in.jplearning.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionDAO extends JpaRepository<Question,Long> {

    @Query("select new com.in.jplearning.dtos.QuestionDTO(q.content,q.exercises.exercisesID,a.answerID,a.answer,a.isCorrect,a.description) from Question q " +
            "left join q.answerList a where q.exercises.exercisesID = ?1")
    List<QuestionDTO> getByExerciseId(Long exerciseID);
}
