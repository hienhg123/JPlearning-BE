package com.in.jplearning.controllers;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.User;
import com.in.jplearning.service.UserService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtAuthFilter jwtAuthFilter;

    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> requestMap) {
        try {
            return userService.register(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody  Map<String, String> requestMap) {
        try {
            return userService.login(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/getAllUser")
    public ResponseEntity<List<User>> getAllUser() {
        try {
            return userService.getAllUser();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(path = "/updateUser")
    public ResponseEntity<String> updateUser(@RequestBody  Map<String, String> requestMap) {
        try {
            return userService.updateUser(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @GetMapping(path = "/profile")
    public ResponseEntity<String> getUserProfile() {
        return userService.getUserProfile();
    }
    @PutMapping("/update-profile/{userId}")
    public ResponseEntity<String> updateProfile(@PathVariable Long userId,
                                                @RequestParam(name = "userPicture", required = false) MultipartFile userPicture,
                                                @RequestParam Map<String, String> requestMap) {

        return userService.updateProfile(userId, userPicture, requestMap);
    }
    @PostMapping(path = "/forgetPassword")
    public ResponseEntity<Map<String,String>> forgetPassword(@RequestBody  Map<String, String> requestMap){
        Map<String, String> response = new HashMap<>();
        response.put("message","Something went wrong");
        try{
            return userService.forgetPassword(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap) {
        try {
            return userService.changePassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    @PostMapping(path = "/validateOtp")
    public ResponseEntity<String> validateOtp(@RequestBody Map<String,String> requestMap){
        try {
            return userService.validateOtp(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PutMapping(path = "/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String,String> requestMap){
        try {
            return userService.resetPassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
