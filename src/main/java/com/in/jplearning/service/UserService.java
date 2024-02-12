package com.in.jplearning.service;

import com.in.jplearning.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {

    ResponseEntity<String> register(Map<String, String> requestMap);
    ResponseEntity<List<User>> getAllUser();

    ResponseEntity<String> login(Map<String, String> requestMap);

    ResponseEntity<String> updateUser(Map<String, String> requestMap);

    ResponseEntity<String> checkToken();

    ResponseEntity<String> changePassword(Map<String, String> requestMap);

    ResponseEntity<String> forgetPassword(Map<String, String> requestMap);

}
