package com.in.jplearning.service;

import com.in.jplearning.model.Course;
import com.in.jplearning.model.CourseFeedBack;
import com.in.jplearning.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CourseService {


    ResponseEntity<List<Course>> getAllCourse();


    ResponseEntity<Course> getByID(Long courseID);

    ResponseEntity<String> enroll(Map<String, String> requestMap);

    ResponseEntity<List<Map<String, Object>>> getAllCourseWithDetails();

    ResponseEntity<Map<String, Object>> getCourseDetailsById(Long courseID);

    ResponseEntity<String> addCourseFeedback(Long courseId, CourseFeedBack feedback);


    ResponseEntity<?> getAllFeedbackForCourse(Long courseId, int pageNumber, int pageSize);


    ResponseEntity<?> createCourse(String courseName, String courseDescription, String courseLevel, Boolean isFree, Boolean isDraft, List<MultipartFile> files, Map<String, Object> chapters);

}
