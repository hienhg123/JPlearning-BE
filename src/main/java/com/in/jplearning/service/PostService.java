package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface PostService {
    ResponseEntity<String> createPost(Map<String, String> requestMap, MultipartFile file) throws IOException;

}
