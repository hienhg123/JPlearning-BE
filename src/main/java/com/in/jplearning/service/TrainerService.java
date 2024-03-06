package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface TrainerService {
    ResponseEntity<String> registerAsTrainer(MultipartFile pictureFiles);
}
