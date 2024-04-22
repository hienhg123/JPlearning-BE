package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.Lesson;
import com.in.jplearning.model.Note;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.CourseEnrollDAO;
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

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteDAO noteDAO;
    private final UserDAO userDAO;
    private final JwtAuthFilter jwtAuthFilter;

    private final CourseEnrollDAO courseEnrollDAO;

    @Override
    public ResponseEntity<?> getAllUserNote() {
        try {
            //get the current user
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            //get all the course id that user have enroll
            List<Course> courseList = courseEnrollDAO.getCourseEnrollByUser(userOptional.get());
            List<Long> courseIDList = new ArrayList<>();
            for(Course course : courseList){
                courseIDList.add(course.getCourseID());
            }
            List<Note> noteList = noteDAO.getAllUserNoteWithEnrolledCourse(courseIDList);
            Map<String, List<Note>> courseNotesMap = new HashMap<>();
            for (Note note : noteList) {
               //get the course name
                String courseName = note.getLesson().getChapter().getCourse().getCourseName();

                // check if added
                if (courseNotesMap.containsKey(courseName)) {
                    // if added ad the new note
                    courseNotesMap.get(courseName).add(note);
                } else {
                    //create new map
                    List<Note> courseNotesList = new ArrayList<>();
                    courseNotesList.add(note);
                    courseNotesMap.put(courseName, courseNotesList);
                }
            }
            return new ResponseEntity<>(courseNotesMap, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    @Transactional
    public ResponseEntity<String> saveNote(Map<String, String> requestMap) {
        try {
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            //get note from map
            Note note = mapToNote(requestMap);
            noteDAO.save(note);
            return JPLearningUtils.getResponseEntity("Lưu thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateNote(Map<String, String> requestMap) {
        try {
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            //get the note
            Optional<Note> note = noteDAO.findById(Long.parseLong(requestMap.get("noteID")));
            //check if note is exist
            if (!note.isEmpty()) {
                note.get().setNote(requestMap.get("note"));
                noteDAO.save(note.get());
                return JPLearningUtils.getResponseEntity("Cập nhật thành công", HttpStatus.OK);
            } else {
                return JPLearningUtils.getResponseEntity("Ghi chú không tồn tại", HttpStatus.OK);
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
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Optional<Note> note = noteDAO.findById(noteID);
            if(note.isEmpty()){
                return JPLearningUtils.getResponseEntity("Ghi chú không tồn tại", HttpStatus.OK);
            }
            noteDAO.deleteById(noteID);
            return JPLearningUtils.getResponseEntity("Xóa thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<?> getByLesson(Long lessonID) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(noteDAO.getUserNoteByLessonID(userOptional.get().getUserID(),lessonID), HttpStatus.OK);
        }catch (Exception ex){
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
