package com.in.jplearning.serviceImpl;

import com.in.jplearning.model.Exercises;
import com.in.jplearning.repositories.ExerciseDAO;
import com.in.jplearning.service.ExerciseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ExerciseServiceImpl implements ExerciseService {
    private final ExerciseDAO exerciseDAO;

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
    public ResponseEntity<Exercises> getExerciseByIdWithGrammarQuestion(Long exerciseID) {
        try{
            return new ResponseEntity<>(exerciseDAO.getExerciseByIdWithGrammarQuestion(exerciseID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Exercises> getExerciseByIdWithReadingQuestion(Long exerciseID) {
        try{
            return new ResponseEntity<>(exerciseDAO.getExerciseByIdWithReadingQuestion(exerciseID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Exercises> getExerciseByIdWithListeningQuestion(Long exerciseID) {
        try{
            return new ResponseEntity<>(exerciseDAO.getExerciseByIdWithListeningQuestion(exerciseID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
