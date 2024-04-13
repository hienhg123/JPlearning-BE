package com.in.jplearning.service;

import com.in.jplearning.model.Chapter;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChapterService {

    ResponseEntity<Chapter> getChapterLesson(Long chapterID);
    ResponseEntity<List<Map<String, Object>>> progressTracking();

    ResponseEntity<?> getCourseProgress(Long courseID);
}
