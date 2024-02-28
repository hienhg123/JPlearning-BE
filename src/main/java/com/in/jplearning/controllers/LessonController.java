package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Lesson;
import com.in.jplearning.service.LessonService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/lesson")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping(path = "/getLessonById/{lessonID}")
    public ResponseEntity<Lesson> getLesson(@PathVariable Long lessonID){
        try{
            return lessonService.getLesson(lessonID);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Lesson(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
