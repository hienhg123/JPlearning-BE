package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.QuestionType;
import com.in.jplearning.model.Exercises;
import com.in.jplearning.model.User;
import com.in.jplearning.model.UserLessonProgress;
import com.in.jplearning.model.User_Exercise;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.repositories.UserExerciseDAO;
import com.in.jplearning.service.UserExerciseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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


    @Override
    public ResponseEntity<String> submitExercise(Map<String, String> requestMap) {
        try{
            //get current user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            int numberOfAttempts = 0;
            //check if user have done this exercise or not
            List<User_Exercise> user_exercise = userExerciseDAO.getByUser(user.getUserID(),Long.parseLong(requestMap.get("exerciseID")));
            if( user_exercise!= null){
                numberOfAttempts = user_exercise.size() + 1;
            }
            //save in database
            userExerciseDAO.save(getDataFromMap(requestMap,user.getUserID(),numberOfAttempts));
            return JPLearningUtils.getResponseEntity("Nộp bài thành công",HttpStatus.OK);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getExerciseInfoByCurrentUser() {
        try {
            // Get the current user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).orElse(null);

            if (user != null) {

                List<User_Exercise> userExercises = userExerciseDAO.getUserExerciseInfo(user.getUserID());

                // Group user exercises by title
                Map<String, List<User_Exercise>> exercisesMap = userExercises.stream()
                        .collect(Collectors.groupingBy(userExercise -> userExercise.getExercises().getTitle()));

                // Process the grouped exercises
                Map<String, List<Map<String, Object>>> userExerciseMap = new HashMap<>();
                for (Map.Entry<String, List<User_Exercise>> entry : exercisesMap.entrySet()) {
                    List<Map<String, Object>> testParts = entry.getValue().stream()
                            .map(userExercise -> {
                                Map<String, Object> testPartInfo = new HashMap<>();
                                testPartInfo.put("submittedAt", userExercise.getSubmittedAt());
                                testPartInfo.put("questionType", userExercise.getQuestionType());
                                testPartInfo.put("numberOfAttempts", userExercise.getNumberOfAttempts());
                                testPartInfo.put("mark", userExercise.getMark());
                                return testPartInfo;
                            })
                            .collect(Collectors.toList());

                    userExerciseMap.put(entry.getKey(), testParts);
                }

                return ResponseEntity.ok(userExerciseMap);

            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyMap());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }





    @Override
    public ResponseEntity<?> getJLPTTestHistory() {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
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
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
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
}
