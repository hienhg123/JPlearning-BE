package com.in.jplearning.service;

import com.in.jplearning.dtos.QuestionDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface QuestionService {
    ResponseEntity<List<QuestionDTO>> getExerciseQuestion(Long exerciseID);
}
