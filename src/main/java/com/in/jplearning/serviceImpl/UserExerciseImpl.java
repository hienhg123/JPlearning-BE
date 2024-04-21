package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.QuestionType;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
import com.in.jplearning.service.UserExerciseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserExerciseImpl implements UserExerciseService {
    
    private final UserExerciseDAO userExerciseDAO;
    
    private final JwtAuthFilter jwtAuthFilter;
    
    private final UserDAO userDAO;

    private final LessonDAO lessonDAO;

    private final UserLessonProgressDAO userLessonProgressDAO;

    private final UserChapterProgressDAO userChapterProgressDAO;


    @Transactional
    @Override
    public ResponseEntity<String> submitExercise(Map<String, String> requestMap) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Optional<Lesson> lessonOptional = lessonDAO.findById(Long.parseLong(requestMap.get("lessonID")));
            List<User_Exercise> user_exercise = userExerciseDAO.getByUser(userOptional.get().getUserID(),
                    Long.parseLong(requestMap.get("exerciseID")));
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Tài khoản không tồn tại", HttpStatus.NOT_FOUND);
            }
            if(lessonOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Bài học không tồn tại", HttpStatus.NOT_FOUND);
            }
            Chapter chapter = lessonDAO.getChapterByLessonId(Long.parseLong(requestMap.get("lessonID")));
            Optional<UserLessonProgress> userLessonProgressOptional = userLessonProgressDAO.getByLessonId(Long.parseLong(requestMap.get("lessonID")),
                    userOptional.get());
            Optional<UserChapterProgress> userChapterProgressOptional = userChapterProgressDAO.getByUserAndExercise(userOptional.get(),
                    chapter);
            boolean isPass = isPass(requestMap);
            UserLessonProgress userLessonProgress;
            if(userLessonProgressOptional.isEmpty()) {
                userLessonProgress = UserLessonProgress.builder()
                        .user(userOptional.get())
                        .lesson(lessonOptional.get())
                        .build();
            } else {
                userLessonProgress = userLessonProgressOptional.get();
            }

            if(isPass){
                userLessonProgress.setIsFinished(true);
            } else {
                userLessonProgress.setIsFinished(false);
            }
            userLessonProgressDAO.save(userLessonProgress);
            long count = userLessonProgressDAO.countFinishedByUserAndChapter(userOptional.get(),chapter);
            UserChapterProgress userChapterProgress;
            if(userChapterProgressOptional.isEmpty()){
                userChapterProgress = UserChapterProgress.builder()
                        .chapter(chapter)
                        .user(userOptional.get())
                        .build();
            } else {
                userChapterProgress = userChapterProgressOptional.get();
            }
            if(count == chapter.getLessonList().size()){
                userChapterProgress.setIsFinished(true);
            } else {
                userChapterProgress.setIsFinished(false);
            }
            userChapterProgressDAO.save(userChapterProgress);
            int numberOfAttempts = 0;
            if( user_exercise!= null){
                numberOfAttempts = user_exercise.size() + 1;
            }
            //save in database
            userExerciseDAO.save(getDataFromMap(requestMap,userOptional.get().getUserID(),numberOfAttempts));
            return JPLearningUtils.getResponseEntity("Nộp bài thành công",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }



    @Override
    public ResponseEntity<List<Map<String, Object>>> getExerciseInfoByCurrentUser() {
        try {
            // Get the current user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).orElse(null);

            if (user != null) {
                List<User_Exercise> userExercises = userExerciseDAO.getUserExerciseInfo(user.getUserID());

                // Group user exercises by title
                Map<String, List<User_Exercise>> exercisesMap = userExercises.stream()
                        .collect(Collectors.groupingBy(User_Exercise::getTitle));

                // Process the grouped exercises
                List<Map<String, Object>> userExerciseList = new ArrayList<>();
                for (List<User_Exercise> exercises : exercisesMap.values()) {
                    // Find the exercise with the highest numberOfAttempts
                    Optional<User_Exercise> highestAttemptsExercise = exercises.stream()
                            .max(Comparator.comparingInt(User_Exercise::getNumberOfAttempts));

                    highestAttemptsExercise.ifPresent(highest -> {
                        Map<String, Object> exerciseInfo = new HashMap<>();
                        exerciseInfo.put("title", highest.getTitle());
                        exerciseInfo.put("numberOfAttempts", highest.getNumberOfAttempts());
                        exerciseInfo.put("exercises", exercises.stream()
                                .filter(e -> e.getNumberOfAttempts() == highest.getNumberOfAttempts())
                                .map(this::mapToExerciseInfo)
                                .collect(Collectors.toList()));
                        userExerciseList.add(exerciseInfo);
                    });
                }

                return ResponseEntity.ok(userExerciseList);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    @Override
    public ResponseEntity<?> getJLPTTestHistory() {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            List<User_Exercise> userExerciseList = userExerciseDAO.getJLPTHistoryByUser(userOptional.get());
            List<Long> exerciseIDList = new ArrayList<>();
            for(User_Exercise user_exercise : userExerciseList){
                exerciseIDList.add(user_exercise.getExercises().getExercisesID());
            }
            Set<Long> exerciseIDSet = new LinkedHashSet<>(exerciseIDList);
            exerciseIDList.clear();
            exerciseIDList.addAll(exerciseIDSet);
            return new ResponseEntity<>(exerciseIDList, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }

    @Override
    public ResponseEntity<String> submitJLPT(Map<String, String> requestMap) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            int numberOfAttempts = 0;
            List<User_Exercise> userExerciseList = userExerciseDAO.getJLPTByUser(
                    userOptional.get(),
                    Long.parseLong(requestMap.get("exerciseID")),
                    QuestionType.valueOf(requestMap.get("questionType"))
            );
            if( userExerciseList!= null){
                numberOfAttempts = userExerciseList.size() + 1;
            }
            User_Exercise userExercise = getUserExerciseWithQuestionType(requestMap,userOptional.get(),numberOfAttempts);
            userExerciseDAO.save(userExercise);
            return JPLearningUtils.getResponseEntity("Nộp bài thành công",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getJLPTHistoryById(Long exerciseID) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.UNAUTHORIZED);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            List<User_Exercise> userExerciseList = userExerciseDAO.getJLPTHistoryById(userOptional.get(),exerciseID);
            Map<QuestionType, List<User_Exercise>> exercisesMap = userExerciseList.stream()
                    .collect(Collectors.groupingBy(User_Exercise::getQuestionType));
            List<Map<String, Object>> historyInfo = new ArrayList<>();
            for (List<User_Exercise> exercises : exercisesMap.values()) {
                // Find the exercise with the highest numberOfAttempts
                Optional<User_Exercise> highestAttemptsExercise = exercises.stream()
                        .max(Comparator.comparingInt(User_Exercise::getNumberOfAttempts));

                highestAttemptsExercise.ifPresent(highest -> {
                    Map<String, Object> exerciseInfo = new HashMap<>();
                    exerciseInfo.put("title", highest.getTitle());
                    exerciseInfo.put("numberOfAttempts", highest.getNumberOfAttempts());
                    exerciseInfo.put("exercises", exercises.stream()
                            .filter(e -> e.getNumberOfAttempts() == highest.getNumberOfAttempts())
                            .map(this::mapToExerciseInfo)
                            .collect(Collectors.toList()));
                    historyInfo.add(exerciseInfo);
                });
            }
            return new ResponseEntity<>(historyInfo, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private User_Exercise getUserExerciseWithQuestionType(Map<String, String> requestMap, User user, int numberOfAttempts) {
        Exercises exercises = new Exercises();
        exercises.setExercisesID(Long.parseLong(requestMap.get("exerciseID")));
        Date submittedAt = parseDate(requestMap.get("submittedAt"));
        return User_Exercise.builder()
                .exercises(exercises)
                .user(user)
                .mark(Integer.parseInt(requestMap.get("mark")))
                .maxPoint(Integer.parseInt(requestMap.get("maxPoint")))
                .numberOfAttempts(numberOfAttempts)
                .questionType(QuestionType.valueOf(requestMap.get("questionType")))
                .submittedAt(submittedAt)
                .build();
    }


    private User_Exercise getDataFromMap(Map<String, String> requestMap, Long userID, int numberOfAttempts) {
        Exercises exercises = new Exercises();
        exercises.setExercisesID(Long.parseLong(requestMap.get("exerciseID")));
        User user = new User();
        user.setUserID(userID);
        Date submittedAt = parseDate(requestMap.get("submittedAt"));
        return User_Exercise.builder()
                .exercises(exercises)
                .user(user)
                .mark(Integer.parseInt(requestMap.get("mark")))
                .maxPoint(Integer.parseInt(requestMap.get("maxPoint")))
                .numberOfAttempts(numberOfAttempts)
                .submittedAt(submittedAt)
                .build();
    }
    private Date parseDate(String submittedAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(submittedAt);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private boolean isPass(Map<String, String> requestMap) {
        int mark = Integer.parseInt(requestMap.get("mark"));
        int maxPoint = Integer.parseInt(requestMap.get("maxPoint"));
        double result = (double) mark / maxPoint;
        return result >= 0.8;
    }
    private Map<String, Object> mapToExerciseInfo(User_Exercise exercise) {
        Map<String, Object> exerciseInfo = new HashMap<>();
        exerciseInfo.put("maxPoint", exercise.getMaxPoint());
        exerciseInfo.put("numberOfAttempts", exercise.getNumberOfAttempts());
        exerciseInfo.put("submittedAt", exercise.getSubmittedAt());
        exerciseInfo.put("questionType", exercise.getQuestionType());
        exerciseInfo.put("mark", exercise.getMark());
        return exerciseInfo;
    }
}
