package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserExerciseService {

    ResponseEntity<String> submitExercise(Map<String, String> requestMap);
    ResponseEntity<List<String>> getExerciseInfoByCurrentUser();
}
