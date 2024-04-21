package com.in.jplearning.service;

import com.in.jplearning.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    ResponseEntity<String> register(Map<String, String> requestMap);
    ResponseEntity<Page<Map<String, Object>>> getAllUsers(int pageNumber, int pageSize);
    ResponseEntity<String> login(Map<String, String> requestMap);
    ResponseEntity<Map<String, Object>> getUserProfile();
    ResponseEntity<String> checkToken();
    ResponseEntity<String> changePassword(Map<String, String> requestMap);
    ResponseEntity<?> forgetPassword(Map<String, String> requestMap);
    ResponseEntity<String> updateProfile(MultipartFile userPicture, Map<String, String> requestMap);

    ResponseEntity<String> updateUser(Map<String, String> requestMap);

    ResponseEntity<?> checkPremium();

    ResponseEntity<?> getThatUserProfile(Long userID);

    ResponseEntity<?> checkThatUserPremium(Long userID);
}
