package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserExerciseService {

    ResponseEntity<String> submitExercise(Map<String, String> requestMap);
    ResponseEntity<List<Map<String, Object>>> getExerciseInfoByCurrentUser();

    ResponseEntity<?> getJLPTTestHistory();

    ResponseEntity<String> submitJLPT(Map<String, String> requestMap);

    ResponseEntity<?> getJLPTHistoryById(Long exerciseID);
}
