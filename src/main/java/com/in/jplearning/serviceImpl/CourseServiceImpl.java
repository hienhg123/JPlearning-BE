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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;


import java.io.IOException;
import java.time.LocalDateTime;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


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

    private final TrainerDAO trainerDAO;

    private final S3AsyncClient s3AsyncClient;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final String cloudFront = "https://d1zyxez9jkrxyw.cloudfront.net";

    private final String bucketName = "jplearning-lesson";


    @Transactional
    @Override
    public ResponseEntity<?> createCourse(String courseName, String courseDescription, String courseLevel, String isFree, String isDraft, List<MultipartFile> files, List<Map<String, Object>> chapters) {
        try {

            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            Trainer trainer = trainerDAO.getByUserId(userOptional.get().getUserID());
            //check if manager or trainer
            if (!jwtAuthFilter.isManager() || trainer == null) {
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            //get the course
            Course course = getCourseFromMap(courseName, courseDescription, courseLevel, isFree,isDraft);
            //get the course chapter
            List<Chapter> chapterList = new ArrayList<>();
            for (Map<String, Object> chapterMap : chapters) {
                Chapter chapter = mapToChapter(chapterMap, course, files);
                chapterList.add(chapter);
            }
            course.setChapterList(chapterList);
            course.setCreateBy(userOptional.get());
            courseDAO.save(course);
            if(Boolean.parseBoolean(isDraft)){
                return JPLearningUtils.getResponseEntity("Tạo bản nháp thành công", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("Tạo thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getUserEnrollCourse() {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(courseEnrollDAO.getCourseEnrollByUser(userOptional.get()), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
       return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> isEnroll(Long courseID) {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            Optional<CourseEnroll> courseEnroll = courseEnrollDAO.findByUserAndCourse(userOptional.get().getUserID(),courseID);
            if(courseEnroll.isEmpty()){
                return JPLearningUtils.getResponseEntity("false", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("true", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getCreatedCourse() {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            Trainer trainer = trainerDAO.getByUserId(userOptional.get().getUserID());
            //check if manager or trainer
            if (!jwtAuthFilter.isManager() || trainer == null) {
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(courseDAO.getCreatedCourse(userOptional.get().getUserID()), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getDraftCourse() {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            Trainer trainer = trainerDAO.getByUserId(userOptional.get().getUserID());
            //check if manager or trainer
            if (!jwtAuthFilter.isManager() || trainer == null) {
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(courseDAO.getDraftCourse(userOptional.get().getUserID()), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteCourse(Long courseID) {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            Optional<Course> courseOptional = courseDAO.findById(courseID);
            List<CourseEnroll> courseEnrolls = courseEnrollDAO.getByCourseID(courseID);
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            Trainer trainer = trainerDAO.getByUserId(userOptional.get().getUserID());
            //check if manager or trainer
            if (!jwtAuthFilter.isManager() || trainer == null) {
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            if(courseOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Khóa học không tồn tại", HttpStatus.NOT_FOUND);
            }
            courseEnrollDAO.deleteAll(courseEnrolls);
            courseDAO.deleteById(courseID);
            return JPLearningUtils.getResponseEntity("Xóa thành công", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Course>> getAllCourse() {
        log.info("Inside getAllCourse");
        try {
            // Retrieve all courses from the database
            List<Course> courses = courseDAO.findAllByIsDraft(false);

            return new ResponseEntity<>(courses, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Course> getByID(Long courseID) {
        try {
            return new ResponseEntity<>(courseDAO.findById(courseID).get(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Course(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> enroll(Map<String, String> requestMap) {
        try {
            //get course
            Course course = courseDAO.findById(Long.parseLong(requestMap.get("courseID"))).get();
            //get user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            //get course enroll
            Optional<CourseEnroll> courseEnroll = courseEnrollDAO.findByUserAndCourse(user.getUserID(), course.getCourseID());
            log.info(String.valueOf(courseEnroll.isPresent()));
            //check if user exist
            if (courseEnroll.isEmpty()) {
                //check if course is free
                if (course.getIsFree()) {
                    //enroll user into course
                    enrollCourse(course, user);
                    return JPLearningUtils.getResponseEntity("Thành công", HttpStatus.OK);
                } else {
                    //check user premium
                    if (isPremiumExpire(user)) {
                        //enroll
                        enrollCourse(course, user);
                        return JPLearningUtils.getResponseEntity("Thành công", HttpStatus.OK);
                    } else {
                        return JPLearningUtils.getResponseEntity("Tài khoản của bạn chưa nâng cấp", HttpStatus.BAD_REQUEST);
                    }
                }

            } else {
                return JPLearningUtils.getResponseEntity("Bạn đã tham gia khóa học này", HttpStatus.BAD_REQUEST);
            }


        } catch (Exception ex) {
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

            Long totalFeedbackCount = courseFeedbackDAO.countByCourse(course);
            courseDetails.put("totalFeedbackCount", totalFeedbackCount);
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
                return JPLearningUtils.getResponseEntity("Không tìm được", HttpStatus.BAD_REQUEST);
            }

            if (feedback.getCreatedAt() == null) {
                feedback.setCreatedAt(LocalDateTime.now()); // Set current timestamp
            }

            // Validate rating
            if (feedback.getRating() < 1 || feedback.getRating() > 5) {
                return JPLearningUtils.getResponseEntity("", HttpStatus.BAD_REQUEST);
            }

            // Obtain the course using courseId
            Optional<Course> courseOptional = courseDAO.findById(courseId);
            if (courseOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Không tìm thấy khóa học ", HttpStatus.BAD_REQUEST);
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

    @Override
    public ResponseEntity<?> getAllFeedbackForCourse(Long courseId, int pageNumber, int pageSize) {
        try {
            // Get the course by its ID
            Optional<Course> courseOptional = courseDAO.findById(courseId);
            if (!courseOptional.isPresent()) {
                return JPLearningUtils.getResponseEntity("Không tìm thấy khóa học", HttpStatus.NOT_FOUND);
            }
            Course course = courseOptional.get();

            // Retrieve feedback for the given course using pagination
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
            Page<CourseFeedBack> feedbackPage = courseFeedbackDAO.findByCourse(course, pageable);

            // Convert the page of feedback entities to a page of feedback information maps
            Page<Map<String, Object>> feedbackPageInfo = feedbackPage.map(feedback -> {
                Map<String, Object> feedbackInfoMap = new HashMap<>();
                feedbackInfoMap.put("comment", feedback.getComment());
                feedbackInfoMap.put("fullName", feedback.getUser().getFirstName() + " " + feedback.getUser().getLastName());
                // You can add more properties to the map if needed
                return feedbackInfoMap;
            });

            // Return the page of feedback information maps
            return ResponseEntity.ok(feedbackPageInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error occurred while fetching feedback for the course.", ex);
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    private void enrollCourse(Course course, User user) {
        CourseEnroll courseEnroll = CourseEnroll.builder()
                .user(user)
                .course(course)
                .joinTime(LocalDateTime.now())
                .build();
        courseEnrollDAO.save(courseEnroll);
    }

    private boolean isPremiumExpire(User user) {
        //get user premium
        List<Bill> bills = billDAO.getUserLatestBill(user.getEmail(), PageRequest.of(0, 1));
        if(bills.isEmpty()){
            return false;
        }
        Bill bill = bills.get(0);
        //check if bill is expire or not
        return bill.getExpireAt().isAfter(LocalDateTime.now());
    }

    private Course getCourseFromMap(String courseName, String courseDescription, String courseLevel, String isFree, String isDraft) {
        return Course.builder()
                .courseName(courseName)
                .courseDescription(courseDescription)
                .courseLevel(JLPTLevel.valueOf(courseLevel))
                .isFree(Boolean.parseBoolean(isFree))
                .isDraft(Boolean.parseBoolean(isDraft))
                .build();

    }


    private Chapter mapToChapter(Map<String, Object> chapters, Course course, List<MultipartFile> files) {
        Chapter chapter = new Chapter();
        chapter.setChapterDescription(chapters.get("chapterDescription").toString());
        chapter.setChapterTitle(chapters.get("chapterTitle").toString());
        List<Map<String, Object>> lessons = (List<Map<String, Object>>) chapters.get("lessonList");
        List<Lesson> lessonList = lessons.stream().map(lessonMap -> {
            try {
                return mapToLesson(lessonMap, chapter, files, course.getCourseName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        chapter.setLessonList(lessonList);
        chapter.setCourse(course);
        return chapter;
    }

    private Lesson mapToLesson(Map<String, Object> lessonMap, Chapter chapter, List<MultipartFile> multipartFiles, String courseName) throws IOException {
        Lesson lesson = new Lesson();
        String random = UUID.randomUUID().toString();
        lesson.setLessonTitle(lessonMap.get("lessonTitle").toString());
        lesson.setLessonDescription(lessonMap.get("lessonDescription").toString());
        List<MultipartFile> upload = new ArrayList<>();
        //get the file of each if not null then set
        if (lessonMap.get("vocabularyMaterial") != null) {
            String key = cloudFront +"/" + courseName + "/chapters/" + chapter.getChapterTitle() + "/" + lesson.getLessonTitle() + "/" + random + "_" + lessonMap.get("vocabularyMaterial");
            lesson.setVocabularyMaterial(key);
        }
        //check listening
        if (lessonMap.get("listeningMaterial") !=null) {
            String key = cloudFront + "/" + courseName + "/chapters/" + chapter.getChapterTitle() + "/" + lesson.getLessonTitle() + "/" + random + "_" + lessonMap.get("listeningMaterial");
            lesson.setListeningMaterial(key);
        }
        //check grammar
        if (lessonMap.get("grammarMaterial") != null) {
            String key = cloudFront + "/" + courseName + "/chapters/" + chapter.getChapterTitle() + "/" + lesson.getLessonTitle() + "/" + random + "_" + lessonMap.get("grammarMaterial");
            lesson.setGrammarMaterial(key);
        }
        //check video
        if (lessonMap.get("videoMaterial") != null) {
            String key = cloudFront + "/" + courseName + "/chapters/" + chapter.getChapterTitle() + "/" + lesson.getLessonTitle() + "/" + random + "_" + lessonMap.get("videoMaterial");
            lesson.setVideoMaterial(key);
        }
        //loop throw each file
        for (MultipartFile file : multipartFiles) {
            //loop to check each key
            for(String key : lessonMap.keySet()){
                //check if equal
                if(Objects.equals(file.getOriginalFilename(), lessonMap.get(key))){
                    upload.add(file);
                    break;
                }
            }
        }
        uploadToS3(upload, random, courseName, chapter.getChapterTitle(), lesson.getLessonTitle());
        lesson.setChapter(chapter);
        return lesson;
    }

    private void uploadToS3(List<MultipartFile> files, String uuid, String courseName, String chapterTitles, String lessonTitles){
        List<CompletableFuture<PutObjectResponse>> uploadFutures = files.stream()
                .map(file -> {
                    try {
                        return uploadToS3Async(file, uuid, courseName, chapterTitles, lessonTitles);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private CompletableFuture<PutObjectResponse> uploadToS3Async(MultipartFile file, String uuid, String courseName, String chapterTitle, String lessonTitle) throws IOException {
        String key = courseName + "/chapters/" + chapterTitle + "/" + lessonTitle + "/" + uuid + "_" + file.getOriginalFilename();
        return s3AsyncClient.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                AsyncRequestBody.fromInputStream(file.getInputStream(), file.getSize(), executorService));
    }






}
