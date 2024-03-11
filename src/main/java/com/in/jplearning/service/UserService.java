package com.in.jplearning.service;

import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    ResponseEntity<String> register(Map<String, String> requestMap);
    ResponseEntity<List<User>> getAllUser();
    ResponseEntity<String> login(Map<String, String> requestMap);
    ResponseEntity<String> getUserProfile();
    ResponseEntity<String> checkToken();
    ResponseEntity<String> changePassword(Map<String, String> requestMap);
    ResponseEntity<Map<String,String>> forgetPassword(Map<String, String> requestMap);
    ResponseEntity<String> updateProfile(MultipartFile userPicture, Map<String, String> requestMap);
    ResponseEntity<String> validateOtp(Map<String, String> requestMap);
    ResponseEntity<String> resetPassword(Map<String, String> requestMap);
}
