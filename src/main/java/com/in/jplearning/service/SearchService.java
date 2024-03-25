package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;

public interface SearchService {
    ResponseEntity<?> searchForCourseAndPosts(String value);
}
