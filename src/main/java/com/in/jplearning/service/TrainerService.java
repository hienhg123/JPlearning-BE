package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface TrainerService {
    ResponseEntity<String> registerAsTrainer(MultipartFile pictureFiles, Map<String, String> requestMap);
}
