package com.in.jplearning.serviceImpl;


import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.Role;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.User;
import com.in.jplearning.repo.CourseDAO;
import com.in.jplearning.repo.UserDAO;
import com.in.jplearning.service.CourseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDAO userDAO;


    @Override
    public ResponseEntity<String> createCourse(Map<String, String> requestMap) {
        log.info("Inside createCourse {}", requestMap);
        try {
            // Check if required information is provided in the requestMap

                // Check if the course with the given name already exists
                if (courseDAO.findByCourseName(requestMap.get("courseName")).isPresent()) {
                    return JPLearningUtils.getResponseEntity("Course already exists", HttpStatus.BAD_REQUEST);
                }

                // Create and save the course
                courseDAO.save(getCourseFromMap(requestMap));

                return JPLearningUtils.getResponseEntity("Course successfully created", HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private Course getCourseFromMap(Map<String, String> requestMap) {
        String levelString = requestMap.get("courseLevel");
        JLPTLevel level = JLPTLevel.valueOf(levelString); // Assuming level names match the enum values

        return Course.builder()
                .courseName(requestMap.get("courseName"))
                .courseDescription(requestMap.get("courseDescription"))
                .courseLevel(level)
                .build();
    }

    @Override
    public ResponseEntity<List<Course>> getAllCourse() {
        log.info("Inside getAllCourse");
        try {
            // Retrieve all courses from the database
            List<Course> courses = courseDAO.findAll();

            return new ResponseEntity<>(courses, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
