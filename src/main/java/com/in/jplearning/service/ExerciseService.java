package com.in.jplearning.service;

import com.in.jplearning.model.Exercises;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ExerciseService {
    ResponseEntity<Exercises> getLessonExerciseByLessonID(Long lessonID);

    ResponseEntity<List<Exercises>> getJLPTTest();

    ResponseEntity<Exercises> getJLPTExerciseByID(Long exerciseID);
}
