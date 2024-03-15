package com.in.jplearning.controllers;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.model.Chapter;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.User;
import com.in.jplearning.service.ChapterService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/chapter")
public class ChapterController {
    private final ChapterService chapterService;
    private final JwtAuthFilter jwtAuthFilter;


    @GetMapping(path = "/getChapterLesson/{chapterID}")
    public ResponseEntity<Chapter> getChapterLesson(@PathVariable Long chapterID) {
        try {
            return chapterService.getChapterLesson(chapterID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Chapter(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/courseProgress")
    public ResponseEntity<List<Map<String, Object>>> getCourseProgress(Principal principal) {
        try {
            String userEmail = jwtAuthFilter.getCurrentUser(); // Assuming this method extracts the user's email from the JWT token
            List<Map<String, Object>> courseProgressList = chapterService.getCoursesWithProgressByUser(userEmail);
            return new ResponseEntity<>(courseProgressList, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
