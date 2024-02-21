package com.in.jplearning.service;

import com.in.jplearning.model.Lesson;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LessonService {
    ResponseEntity<List<Lesson>> getLessonByLessonOrderAndChapterID(Long chapterID);

    ResponseEntity<Lesson> getLesson(Long chapterID, Integer lessonOrder);
}
