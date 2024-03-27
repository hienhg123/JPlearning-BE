package com.in.jplearning.service;

import com.in.jplearning.model.Exercises;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ExerciseService {
    ResponseEntity<Exercises> getLessonExerciseByLessonID(Long lessonID);

    ResponseEntity<List<Exercises>> getJLPTTest();


    ResponseEntity<?> getExerciseByIdWithReadingQuestion(Long exerciseID);

    ResponseEntity<?> getExerciseByIdWithListeningQuestion(Long exerciseID);

    ResponseEntity<?> getExerciseByIdWithGrammarQuestion(Long exerciseID);
}
