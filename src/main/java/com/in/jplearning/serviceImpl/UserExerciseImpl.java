package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.ExerciseType;
import com.in.jplearning.model.Exercises;
import com.in.jplearning.model.User;
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
            log.info(requestMap.get("exerciseID"));
            List<User_Exercise> user_exercise = userExerciseDAO.getByUser(user.getUserID(),Long.parseLong(requestMap.get("exerciseID")));
            if( user_exercise!= null){
                numberOfAttempts = user_exercise.size() + 1;
            }
            //save in database
            userExerciseDAO.save(getDataFromMap(requestMap,user.getUserID(),numberOfAttempts));
            return JPLearningUtils.getResponseEntity("Successfully saved",HttpStatus.OK);

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
                List<Object[]> result = userExerciseDAO.getUserExerciseInfo(user.getUserID());

                // Process the result and create the desired response
                List<Map<String, Object>> userExerciseList = result.stream()
                        .map(entry -> {
                            Map<String, Object> userExerciseInfo = new HashMap<>();
                            userExerciseInfo.put("title", (String) entry[3]);
                            userExerciseInfo.put("mark", (int) entry[0]);
                            userExerciseInfo.put("submittedAt", (Date) entry[1]);
                            userExerciseInfo.put("numberOfAttempts", (int) entry[2]);
                            return userExerciseInfo;
                        })
                        .collect(Collectors.toList());

                return ResponseEntity.ok(userExerciseList);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
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
