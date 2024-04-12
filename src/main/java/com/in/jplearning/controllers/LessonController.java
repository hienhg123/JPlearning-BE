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
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/lesson")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping(path = "/getLessonById")
    public ResponseEntity<?> getLesson(@RequestParam("isFree") String isFree, @RequestParam("lessonId") String lessonId){
        try{
            return lessonService.getLesson(isFree,lessonId);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping(path = "/findCourseByLessonID/{lessonID}")
    public ResponseEntity<?> findCourseByLessonID(@PathVariable Long lessonID){
        try{
            return lessonService.findCourseByLessonID(lessonID);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
