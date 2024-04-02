package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
import com.in.jplearning.service.ChapterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
@AllArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterDAO chapterDAO;
    private final UserLessonProgressDAO userLessonProgressDAO;
    private final UserChapterProgressDAO userChapterProgressDAO;
    private final UserDAO userDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final CourseDAO courseDAO;
    private final CourseEnrollDAO courseEnrollDAO;

    @Override
    public ResponseEntity<Chapter> getChapterLesson(Long chapterID) {
        try{
            return new ResponseEntity<>(chapterDAO.getChapterLessonByOrder(chapterID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Chapter(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> progressTracking() {
        try {
            List<Map<String, Object>> courseDetailsList = new ArrayList<>();
            List<Course> courses = courseDAO.findAll();

            for (Course course : courses) {
                Map<String, Object> courseDetails = new HashMap<>();
                courseDetails.put("courseName", course.getCourseName());

                // Calculate the count of enrolled users for each course
                Long enrollCount = courseEnrollDAO.countByCourse(course);
                courseDetails.put("enrolledUsersCount", enrollCount);

                // Calculate the progress for each course
                double progress = calculateCourseProgress(course);
                courseDetails.put("courseProgress", progress);

                courseDetailsList.add(courseDetails);
            }

            return new ResponseEntity<>(courseDetailsList, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private double calculateCourseProgress(Course course) {
        int totalChapters = course.getChapterList().size();
        int completedChapters = 0;

        for (Chapter chapter : course.getChapterList()) {
            List<UserChapterProgress> userChapterProgressList = userChapterProgressDAO.findByChapter(chapter);
            boolean chapterCompleted = false;

            for (UserChapterProgress userChapterProgress : userChapterProgressList) {
                if (userChapterProgress.getIsFinished()) {
                    chapterCompleted = true;
                    break; // Break the loop if at least one user has finished the chapter
                }
            }

            if (chapterCompleted) {
                completedChapters++;
            }
        }

        double progress = totalChapters > 0 ? (double) completedChapters / totalChapters : 0.0;
        // Cap progress at 100%
        return Math.min(((double) completedChapters / totalChapters) * 100, 100.0);
    }

}

