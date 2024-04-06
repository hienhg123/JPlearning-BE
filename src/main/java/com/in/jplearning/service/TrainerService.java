package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TrainerService {
    ResponseEntity<String> registerAsTrainer(List<MultipartFile> pictureFiles, Map<String, String> requestMap);

    ResponseEntity<String> updateStatus(Map<String, String> requestMap);

    ResponseEntity<?> getAllTrainer(int pageNumber, int pageSize);

    ResponseEntity<String> updateTrainerStatus(Map<String, String> requestMap);

    ResponseEntity<String> checkTrainer();
}
