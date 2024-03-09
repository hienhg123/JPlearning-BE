package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Course;
import com.in.jplearning.service.CourseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(path = "/course")
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping(path = "/courseCreate")
    public ResponseEntity<String> courseCreate(@RequestBody Map<String, String> requestMap) {
        try {
            return courseService.createCourse(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @GetMapping(path = "/getAllCourses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return courseService.getAllCourse();
    }
    @GetMapping(path = "/getCourse/{courseID}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long courseID){
        try{
            return courseService.getByID(courseID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Course(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping(path = "/enrollCourse")
    public ResponseEntity<String> enrollCourse(@RequestBody Map<String,String> requestMap){
        try{
            return courseService.enroll(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
