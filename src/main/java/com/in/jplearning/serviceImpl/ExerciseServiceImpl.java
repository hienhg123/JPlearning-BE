package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Exercises;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.ExerciseDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.ExerciseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {
    private final ExerciseDAO exerciseDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<Exercises> getLessonExerciseByLessonID(Long lessonID) {
        try{
            return new ResponseEntity<>(exerciseDAO.getLessonExerciseByLessonID(lessonID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Exercises>> getJLPTTest() {
        try{
            return new ResponseEntity<>(exerciseDAO.getJLPTTest(), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getExerciseByIdWithGrammarQuestion(Long exerciseID) {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            //check if user login or not
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(exerciseDAO.getExerciseByIdWithGrammarQuestion(exerciseID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getExerciseByIdWithReadingQuestion(Long exerciseID) {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            //check if user login or not
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(exerciseDAO.getExerciseByIdWithReadingQuestion(exerciseID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getExerciseByIdWithListeningQuestion(Long exerciseID) {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            //check if user login or not
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(exerciseDAO.getExerciseByIdWithListeningQuestion(exerciseID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
