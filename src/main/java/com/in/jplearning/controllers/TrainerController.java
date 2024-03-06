package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.service.TrainerService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping(path = "/trainer")
@AllArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping("/register-as-trainer")
    public ResponseEntity<String> registerAsTrainer(@RequestParam("files") MultipartFile pictureFiles){
        try{
            return trainerService.registerAsTrainer(pictureFiles);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
