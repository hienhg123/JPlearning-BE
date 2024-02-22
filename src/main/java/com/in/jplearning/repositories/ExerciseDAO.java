package com.in.jplearning.repositories;

import com.in.jplearning.model.Exercises;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseDAO extends JpaRepository<Exercises,Long> {

    @Query("select e from Exercises e where e.lesson.lessonID =?1")
    Exercises getLessonExerciseByLessonID(Long lessonID);

    @Query("select e from Exercises e where e.exerciseType = 'JLPT_TEST'")
    List<Exercises> getJLPTTest();

    @Query("select e from Exercises e where e.exercisesID =?1 and e.exerciseType = 'JLPT_TEST'")
    Exercises getJLPTExerciseByID(Long exerciseID);
}
