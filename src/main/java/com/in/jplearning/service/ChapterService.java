package com.in.jplearning.service;

import com.in.jplearning.model.Chapter;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ChapterService {
    ResponseEntity<List<Chapter>> getAllChapterByCourseID(Long courseID);
}
