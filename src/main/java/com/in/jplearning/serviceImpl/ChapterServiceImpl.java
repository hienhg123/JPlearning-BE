package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
import com.in.jplearning.service.ChapterService;
import com.in.jplearning.utils.JPLearningUtils;
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
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();

            List<Map<String, Object>> courseDetailsList = new ArrayList<>();

            List<Course> enrolledCourses = courseEnrollDAO.getCourseEnrollByUser(user);

            for (Course course : enrolledCourses) {
                Map<String, Object> courseDetails = new HashMap<>();
                courseDetails.put("courseID", course.getCourseID());
                courseDetails.put("courseName", course.getCourseName());

                // Calculate the count of enrolled users for each course
                Long enrollCount = courseEnrollDAO.countByCourse(course);
                courseDetails.put("enrolledUsersCount", enrollCount);

                // Calculate the progress for each course
                double progress = calculateCourseProgress(course,user);
                courseDetails.put("courseProgress", progress);

                courseDetailsList.add(courseDetails);
            }

            return new ResponseEntity<>(courseDetailsList, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getCourseProgress(Long courseID) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.UNAUTHORIZED);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Optional<Course> courseOptional = courseDAO.findById(courseID);
            if(courseOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Khóa học không tồn tại", HttpStatus.NOT_FOUND);
            }
            double progress = calculateCourseProgress(courseOptional.get(),userOptional.get());
            return new ResponseEntity<>(progress, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private double calculateCourseProgress(Course course, User user) {
        int totalChapters = course.getChapterList().size();
        if (totalChapters == 0) {
            return 0.0;
        }

        int completedChapters = 0;

        for (Chapter chapter : course.getChapterList()) {
            List<UserChapterProgress> userChapterProgressList = userChapterProgressDAO.findByChapterAndUser(chapter, user);
            boolean chapterCompleted = false;

            for (UserChapterProgress userChapterProgress : userChapterProgressList) {
                if (userChapterProgress.getIsFinished()) {
                    chapterCompleted = true;
                    break; // Break the loop if the user has finished the chapter
                }
            }

            if (chapterCompleted) {
                completedChapters++;
            }
        }

        double progress = (double) completedChapters / totalChapters;
        // Cap progress at 100%
        double cappedProgress = Math.min(progress * 100, 100.0);
        // Round the capped progress to the nearest integer
        return Math.round(cappedProgress);
    }



}

