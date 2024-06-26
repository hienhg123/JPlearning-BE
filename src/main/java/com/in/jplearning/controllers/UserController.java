package com.in.jplearning.controllers;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.User;
import com.in.jplearning.service.UserService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<String> login(@RequestBody Map<String, String> requestMap) {
        try {
            return userService.login(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/getAllUser/{pageNumber}/{pageSize}")
    public ResponseEntity<Page<Map<String, Object>>> getAllUsers(
            @PathVariable int pageNumber, @PathVariable int pageSize) {
        return userService.getAllUsers(pageNumber, pageSize);
    }

    @GetMapping(path = "/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {

        return userService.getUserProfile();
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<String> updateProfile(@RequestPart(name = "userPicture", required = false) MultipartFile userPicture,
                                                @RequestParam Map<String, String> requestMap) {
        return userService.updateProfile(userPicture, requestMap);
    }

    @PostMapping(path = "/forgetPassword")
    public ResponseEntity<?> forgetPassword(@RequestBody Map<String, String> requestMap) {
        try {
            return userService.forgetPassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
       return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> requestMap) {
        try {
            return userService.changePassword(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/checkToken")
    public ResponseEntity<String> checkToken() {
        try {
            return userService.checkToken();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/userManagement")
    public ResponseEntity<String> updateUser(@RequestBody Map<String, String> requestMap) {
        return userService.updateUser(requestMap);
    }
    @GetMapping("/checkPremium")
    public ResponseEntity<?> checkPremium() {
        try {
            return userService.checkPremium();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/getThatUserProfile/{userID}")
    public ResponseEntity<?> getThatUserProfile(@PathVariable Long userID) {
        try {
            return userService.getThatUserProfile(userID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/checkThatUserPremium/{userID}")
    public ResponseEntity<?> checkThatUserPremium(@PathVariable Long userID) {
        try {
            return userService.checkThatUserPremium(userID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
