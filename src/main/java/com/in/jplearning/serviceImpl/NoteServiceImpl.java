package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Lesson;
import com.in.jplearning.model.Note;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.NoteDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.NoteService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteDAO noteDAO;
    private final UserDAO userDAO;
    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<List<Note>> getAllUserNote() {
        try {
            //get the current user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            log.info("id:" + user.getUserID());
            //get note by user idlog.info();
            return new ResponseEntity<>(noteDAO.getAllUserNote(user.getUserID()), HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    @Transactional
    public ResponseEntity<String> saveNote(Map<String, String> requestMap) {
        try {
            //get note from map
            Note note = mapToNote(requestMap);
            noteDAO.save(note);
            return JPLearningUtils.getResponseEntity("Save successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateNote(Map<String, String> requestMap) {
        try {
            //get the note
            Optional<Note> note = noteDAO.findById(Long.parseLong(requestMap.get("noteID")));
            //check if note is exist
            if (!note.isEmpty()) {
                note.get().setNote(requestMap.get("note"));
                noteDAO.save(note.get());
                return JPLearningUtils.getResponseEntity("Update success fully", HttpStatus.OK);
            } else {
                return JPLearningUtils.getResponseEntity("Note not exist", HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Note> goToLesson(Long noteID) {
        try {
            return new ResponseEntity<>(noteDAO.findById(noteID).get(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Note(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteNote(Long noteID) {
        try {
            noteDAO.deleteById(noteID);
            return JPLearningUtils.getResponseEntity("Note deleted", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private Note mapToNote(Map<String, String> requestMap) {
        //check the current lesson
        Lesson lesson = new Lesson();
        lesson.setLessonID(Long.valueOf(requestMap.get("lessonID")));
        //check the current user
        User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();

        //save note
        Note note = Note.builder()
                .note(requestMap.get("note"))
                .timePoint(Integer.parseInt(requestMap.get("timePoint")))
                .lesson(lesson)
                .user(user)
                .build();
        return note;
    }
}
