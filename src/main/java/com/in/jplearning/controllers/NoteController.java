package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Note;
import com.in.jplearning.service.NoteService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(path = "/note")
@AllArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Note>> getAllUserNote() {
        try {
            return noteService.getAllUserNote();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/createNote")
    public ResponseEntity<String> saveNote(@RequestBody Map<String, String> requestMap) {
        try {
            return noteService.saveNote(requestMap);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/updateNote")
    public ResponseEntity<String> updateNote(@RequestBody Map<String, String> requestMap) {
        try {
            return noteService.updateNote(requestMap);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/getByLesson/{noteID}")
    public ResponseEntity<Note> goToLesson(@PathVariable Long noteID) {
        try {
            return noteService.goToLesson(noteID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Note(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/deleteNote/{noteID}")
    public ResponseEntity<String> deleteNote(@PathVariable Long noteID) {
        try {
            return noteService.deleteNote(noteID);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

