package com.in.jplearning.service;

import com.in.jplearning.model.Chapter;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ChapterService {

    ResponseEntity<Chapter> getChapterLesson(Long chapterID);
}
