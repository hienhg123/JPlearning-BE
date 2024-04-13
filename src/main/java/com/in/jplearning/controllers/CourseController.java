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
                                          @RequestParam("isFree") String isFree,
                                          @RequestParam("isDraft") String isDraft,
                                          @RequestPart(value = "img",required = false) MultipartFile img,
                                          @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                          @RequestParam("chapters") String chaptersJson){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> chapters = objectMapper.readValue(chaptersJson, new TypeReference<List<Map<String, Object>>>() {});
            return courseService.createCourse(courseName,courseDescription,courseLevel,isFree,isDraft,img,files,chapters);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping(path = "/updateCourse")
    public ResponseEntity<?> updateCourse(@RequestParam("courseID") String courseID,
                                          @RequestParam("courseName") String courseName,
                                          @RequestParam("courseDescription") String courseDescription,
                                          @RequestParam("courseLevel") String courseLevel,
                                          @RequestParam("isFree") String isFree,
                                          @RequestParam("isDraft") String isDraft,
                                          @RequestPart(value = "img",required = false) MultipartFile img,
                                          @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                          @RequestParam("chapters") String chaptersJson,
                                          @RequestParam("chapterIdList") String chapterIdListJson,
                                          @RequestParam("lessonIdList") String lessonIdListJson
                                          ){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> chapters = objectMapper.readValue(chaptersJson, new TypeReference<List<Map<String, Object>>>() {});
            List<Long> chapterIdList = objectMapper.readValue(chapterIdListJson, new TypeReference<List<Long>>() {});
            List<Long> lessonIdList = objectMapper.readValue(lessonIdListJson, new TypeReference<List<Long>>() {});
            return courseService.updateCourse(courseID,courseName,courseDescription,courseLevel,isFree,
                    isDraft,img,files,chapters,chapterIdList,lessonIdList);
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

    @GetMapping(path = "/feedback/{courseId}/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getAllFeedback(@PathVariable Long courseId, @PathVariable int pageNumber, @PathVariable int pageSize) {
        try {
            return courseService.getAllFeedbackForCourse(courseId, pageNumber, pageSize);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(path = "/getUserEnrollCourse")
    public ResponseEntity<?> getUserEnrollCourse() {
        try {
            return courseService.getUserEnrollCourse();
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(path = "/getThatUserEnrollCourse/{userID}")
    public ResponseEntity<?> getThatUserEnrollCourse(@PathVariable Long userID) {
        try {
            return courseService.getThatUserEnrollCourse(userID);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(path = "/isEnroll/{courseID}")
    public ResponseEntity<?> isEnroll(@PathVariable Long courseID) {
        try {
            return courseService.isEnroll(courseID);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(path = "/getCreatedCourse")
    public ResponseEntity<?> getCreatedCourse() {
        try {
            return courseService.getCreatedCourse();
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(path = "/getDraftCourse")
    public ResponseEntity<?> getDraftCourse() {
        try {
            return courseService.getDraftCourse();
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping(path = "/deleteCourse/{courseID}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseID) {
        try {
            return courseService.deleteCourse(courseID);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
