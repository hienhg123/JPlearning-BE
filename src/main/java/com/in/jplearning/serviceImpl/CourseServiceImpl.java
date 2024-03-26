package com.in.jplearning.serviceImpl;


import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.BillDAO;
import com.in.jplearning.repositories.CourseDAO;
import com.in.jplearning.repositories.CourseEnrollDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.CourseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    private final BillDAO billDAO;

    private final CourseEnrollDAO courseEnrollDAO;



    @Override
    public ResponseEntity<String> createCourse(Map<String, String> requestMap) {
        log.info("Inside createCourse {}", requestMap);
        try {
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

    @Override
    public ResponseEntity<Course> getByID(Long courseID) {
        try{
            return new ResponseEntity<>(courseDAO.findById(courseID).get(), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Course(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> enroll(Map<String, String> requestMap) {
        try{
            //get course
            Course course = courseDAO.findById(Long.parseLong(requestMap.get("courseID"))).get();
            //get user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            //get course enroll
            Optional<CourseEnroll> courseEnroll = courseEnrollDAO.findByUserAndCourse(user.getUserID(),course.getCourseID());
            log.info(String.valueOf(courseEnroll.isPresent()));
            //check if user exist
            if(!courseEnroll.isPresent()){
                //check if course is free
                if(course.getIsFree() == true){
                    //enroll user into course
                    enrollCourse(course,user);
                    return JPLearningUtils.getResponseEntity("Thành công", HttpStatus.OK);
                } else {
                    //check user premium
                    if(isPremiumExpire(user)){
                        //enroll
                        enrollCourse(course,user);
                        return JPLearningUtils.getResponseEntity("Thành công", HttpStatus.OK);
                    } else {
                        return JPLearningUtils.getResponseEntity("Tài khoản của bạn chưa nâng cấp", HttpStatus.BAD_REQUEST);
                    }
                }

            } else{
                return JPLearningUtils.getResponseEntity("Bạn đã tham gia khóa học này",HttpStatus.BAD_REQUEST);
            }


        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void enrollCourse(Course course, User user) {
       CourseEnroll courseEnroll = CourseEnroll.builder()
               .user(user)
               .course(course)
               .build();
       courseEnrollDAO.save(courseEnroll);
    }

    private boolean isPremiumExpire(User user) {
        //get user premium
        List<Bill> bill = billDAO.getUserLatestBill(user.getEmail(),PageRequest.of(0,1));
        if(bill.isEmpty()){
            return true;
        }
        //check duration
        if(bill.get(0).getExpireAt().isAfter(LocalDateTime.now())){
            return true;
        }
        return false;
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
}
