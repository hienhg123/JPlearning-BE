package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.service.TrainerService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
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
    public ResponseEntity<String> registerAsTrainer(@RequestPart("pictureFiles") List<MultipartFile> pictureFiles,
                                                    @RequestParam Map<String, String> requestMap){
        try{
            return trainerService.registerAsTrainer(pictureFiles,requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestBody Map<String,String> requestMap){
        try{
            return trainerService.updateStatus(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/getAllTrainer/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getAllTrainer(@PathVariable int pageNumber, @PathVariable int pageSize){
        try{
            return trainerService.getAllTrainer(pageNumber,pageSize);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PutMapping("/updateTrainerStatus")
    public ResponseEntity<String> updateTrainerStatus(@RequestBody Map<String,String> requestMap){
        try{
            return trainerService.updateTrainerStatus(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
