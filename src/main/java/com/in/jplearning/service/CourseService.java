package com.in.jplearning.service;

import com.in.jplearning.model.Course;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CourseService {
    ResponseEntity<String> createCourse(Map<String, String> requestMap);

    ResponseEntity<List<Course>> getAllCourse();


    ResponseEntity<Course> getByID(Long courseID);

    ResponseEntity<String> enroll(Map<String, String> requestMap);
}
