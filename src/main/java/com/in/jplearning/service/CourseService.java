package com.in.jplearning.service;

import com.in.jplearning.model.Course;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CourseService {


    ResponseEntity<List<Course>> getAllCourse();


    ResponseEntity<Course> getByID(Long courseID);

    ResponseEntity<String> enroll(Map<String, String> requestMap);


    ResponseEntity<?> createCourse(String courseName, String courseDescription, String courseLevel, Boolean isFree, List<MultipartFile> files, Map<String, Object> chapters);
}
