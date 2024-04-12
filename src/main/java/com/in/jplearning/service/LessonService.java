package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface LessonService {

    ResponseEntity<?> getLesson(String isFree, String lessonId);

    ResponseEntity<?> findCourseByLessonID(Long lessonID);


}
