package com.in.jplearning.controllers;

import com.in.jplearning.model.Exercises;
import com.in.jplearning.service.ExerciseService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/practice/exercise")
public class ExerciseController {
    private final ExerciseService exerciseService;

    @GetMapping("/getLessonExercise/{lessonID}")
    public ResponseEntity<Exercises> getLessonExerciseByLessonID(@PathVariable Long lessonID){
        try{
            return exerciseService.getLessonExerciseByLessonID(lessonID);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/getJLPTExercise")
    public ResponseEntity<List<Exercises>> getJLPTTest(){
        try{
            return exerciseService.getJLPTTest();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/getJLPTExercise/{exerciseID}/grammar")
    public ResponseEntity<Exercises> getExerciseByIdWithGrammarQuestion(@PathVariable Long exerciseID){
        try{
            return exerciseService.getExerciseByIdWithGrammarQuestion(exerciseID);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping(path = "/getJLPTExercise/{exerciseID}/reading")
    public ResponseEntity<Exercises> getExerciseByIdWithReadingQuestion(@PathVariable Long exerciseID){
        try{
            return exerciseService.getExerciseByIdWithReadingQuestion(exerciseID);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping(path = "/getJLPTExercise/{exerciseID}/listening")
    public ResponseEntity<Exercises> getExerciseByIdWithListeningQuestion(@PathVariable Long exerciseID){
        try{
            return exerciseService.getExerciseByIdWithListeningQuestion(exerciseID);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Exercises(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

