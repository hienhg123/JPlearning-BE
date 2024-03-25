package com.in.jplearning.serviceImpl;


import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
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
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseDAO courseDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    private final BillDAO billDAO;

    private final CourseEnrollDAO courseEnrollDAO;
    private final CourseFeedbackDAO courseFeedbackDAO;



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


    @Override
    public ResponseEntity<List<Map<String, Object>>> getAllCourseWithDetails() {
        try {
            List<Map<String, Object>> coursesDetails = new ArrayList<>();
            List<Course> courses = courseDAO.findAll();

            for (Course course : courses) {
                Map<String, Object> courseDetails = new HashMap<>();
                // Calculate the count of enrolled users for each course
                Long enrollCount = courseEnrollDAO.countByCourse(course);
                courseDetails.put("enrolledUsersCount", enrollCount);

                // Calculate the average rating for each course
                Double averageRating = courseFeedbackDAO.calculateAverageRating(course);
                courseDetails.put("averageRating", averageRating);

                coursesDetails.add(courseDetails);
            }

            return new ResponseEntity<>(coursesDetails, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCourseDetailsById(Long courseID) {
        try {
            Course course = courseDAO.findById(courseID).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Map<String, Object> courseDetails = new HashMap<>();
            // Calculate the count of enrolled users for the course
            Long enrollCount = courseEnrollDAO.countByCourse(course);
            courseDetails.put("enrolledUsersCount", enrollCount);

            // Calculate the average rating for the course
            Double averageRating = courseFeedbackDAO.calculateAverageRating(course);
            courseDetails.put("averageRating", averageRating);

            return new ResponseEntity<>(courseDetails, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> addCourseFeedback(Long courseId, CourseFeedBack feedback) {
        try {
            // Obtain the user using the email
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            if (user == null) {
                return JPLearningUtils.getResponseEntity("User not found", HttpStatus.BAD_REQUEST);
            }

            if (feedback.getCreatedAt() == null) {
                feedback.setCreatedAt(LocalDateTime.now()); // Set current timestamp
            }

            // Validate rating
            if (feedback.getRating() < 1 || feedback.getRating() > 5) {
                return JPLearningUtils.getResponseEntity("Rating must be between 1 and 5", HttpStatus.BAD_REQUEST);
            }

            // Obtain the course using courseId
            Optional<Course> courseOptional = courseDAO.findById(courseId);
            if (!courseOptional.isPresent()) {
                return JPLearningUtils.getResponseEntity("Course not found", HttpStatus.BAD_REQUEST);
            }
            Course course = courseOptional.get();

            // Set the user and course for the feedback
            feedback.setUser(user);
            feedback.setCourse(course);

            // Save the feedback
            courseFeedbackDAO.save(feedback);
            return JPLearningUtils.getResponseEntity("Đánh giá thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
        if(bill.get(0).getExpireAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(LocalDate.now())){
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
