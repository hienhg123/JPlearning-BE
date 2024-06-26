package com.in.jplearning.service;

import com.in.jplearning.model.Note;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NoteService {

    ResponseEntity<?> getAllUserNote();

    ResponseEntity<String> saveNote(Map<String, String> requestMap);

    ResponseEntity<String> updateNote(Map<String, String> requestMap);

    ResponseEntity<Note> goToLesson(Long noteID);

    ResponseEntity<String> deleteNote(Long noteID);

    ResponseEntity<?> getByLesson(Long lessonID);
}
