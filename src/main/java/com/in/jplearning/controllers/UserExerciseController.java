package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.service.UserExerciseService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/practice/submit")
public class UserExerciseController {

    private final UserExerciseService userExerciseService;

    @PostMapping("/submitExam")
    public ResponseEntity<String> submitExercise(@RequestBody Map<String,String> requestMap){
        try{
            return userExerciseService.submitExercise(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/examHistory")
    public ResponseEntity<List<Map<String, Object>>> getExerciseInfoByCurrentUser() {
        return userExerciseService.getExerciseInfoByCurrentUser();
    }

    @GetMapping("/getJLPTTestHistory")
    public ResponseEntity<?> submitExercise(){
        try{
            return userExerciseService.getJLPTTestHistory();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping("/submitJLPT")
    public ResponseEntity<String> submitJLPT(@RequestBody Map<String,String> requestMap){
        try{
            return userExerciseService.submitJLPT(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/getJLPTHistoryById/{exerciseID}")
    public ResponseEntity<?> getJLPTHistoryById(@PathVariable Long exerciseID){
        try{
            return userExerciseService.getJLPTHistoryById(exerciseID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
