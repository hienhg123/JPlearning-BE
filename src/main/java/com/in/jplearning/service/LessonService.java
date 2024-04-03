package com.in.jplearning.service;

import com.in.jplearning.model.Lesson;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LessonService {

    ResponseEntity<?> getLesson(Long lessonID);

    ResponseEntity<?> findCourseByLessonID(Long lessonID);
}
