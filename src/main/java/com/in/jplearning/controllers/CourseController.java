package com.in.jplearning.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Chapter;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.CourseFeedBack;
import com.in.jplearning.service.CourseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(path = "/course")
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;

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

    @PostMapping(path = "/createCourse")
    public ResponseEntity<?> createCourse(@RequestParam("courseName") String courseName,
                                          @RequestParam("courseDescription") String courseDescription,
                                          @RequestParam("courseLevel") String courseLevel,
                                          @RequestParam("isFree") Boolean isFree,
                                          @RequestParam("isFree") Boolean isDraft,
                                          @RequestParam("files") List<MultipartFile> files,
                                          @RequestParam("chapters") String chaptersJson){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> chapters = objectMapper.readValue(chaptersJson, new TypeReference<>() {
            });
            return courseService.createCourse(courseName,courseDescription,courseLevel,isFree,isDraft,files,chapters);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/rating")
    public ResponseEntity<List<Map<String, Object>>> getAllCourseWithDetails() {
        return courseService.getAllCourseWithDetails();
    }

    @GetMapping("/rating/{courseID}")
    public ResponseEntity<Map<String, Object>> getCourseDetailsById(@PathVariable Long courseID) {
        return courseService.getCourseDetailsById(courseID);
    }

    @PostMapping("/add/{courseId}")
    public ResponseEntity<String> addCourseFeedback(@PathVariable Long courseId,
                                                    @RequestBody CourseFeedBack feedback
                                                    ) {
        return courseService.addCourseFeedback(courseId, feedback);
    }

}
