package com.in.jplearning.controllers;

import com.in.jplearning.dtos.QuestionDTO;
import com.in.jplearning.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(path = "/question")
@AllArgsConstructor
public class QuestionController {

    private final QuestionService questionService;



    @GetMapping(path = "/getByExercise/{exerciseID}")
    public ResponseEntity<List<QuestionDTO>> getExerciseQuestion(@PathVariable Long exerciseID){
        try{
            return questionService.getExerciseQuestion(exerciseID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
