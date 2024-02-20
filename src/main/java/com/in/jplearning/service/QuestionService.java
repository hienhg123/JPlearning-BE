package com.in.jplearning.service;

import com.in.jplearning.dtos.QuestionDTO;
import com.in.jplearning.model.Question;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface QuestionService {
    ResponseEntity<List<Question>> getExerciseQuestion(Long exerciseID);

    ResponseEntity<List<Question>> getAll();
}
