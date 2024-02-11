package com.in.jplearning.serviceImpl;


import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.config.JwtUtil;
import com.in.jplearning.constants.JPConstants;

import com.in.jplearning.enums.Role;
import com.in.jplearning.model.User;
import com.in.jplearning.repo.UserDAO;
import com.in.jplearning.service.UserService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import com.in.jplearning.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<String> register(Map<String, String> requestMap) {
        log.info("Inside sign up {}", requestMap);
        try {
            //check xem co null hay khong
            if (validateSignUpMap(requestMap)) {
                //check email co hay khong
                if (!userDAO.findByEmail(requestMap.get("email")).isPresent()) {
                    userDAO.save(getUserFromMap(requestMap));
                    return JPLearningUtils.getResponseEntity("Successfully register", HttpStatus.OK);
                } else {
                    return JPLearningUtils.getResponseEntity("Email already exist", HttpStatus.BAD_REQUEST);
                }
            } else {
                return JPLearningUtils.getResponseEntity(JPConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<User>> getAllUser() {
        log.info("Insite getALlUser");
        log.info(String.valueOf(jwtAuthFilter.isAdmin()));
        try {
            //check if person login is admin or not
            if (jwtAuthFilter.isAdmin()) {
                return new ResponseEntity<>(userDAO.findByRole(Role.USER), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        User user = userDAO.findByEmail(requestMap.get("email")).get();
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email")
                            , requestMap.get("password"))
            );
            //check if login in or not
            if (auth.isAuthenticated()) {
                //check status
                if (user.isActive()) {
                    return new ResponseEntity<String>("{\"token\":\"" +
                            jwtUtil.generateToken(user.getUsername(), user.getRole()) + "\"}", HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("Wait for admin approval", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Check Your Password" + "\"}"
                , HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<String> updateUser(Map<String, String> requestMap) {
        try {
            // Check if 'id' is present and not null
            if (requestMap.containsKey("id") && requestMap.get("id") != null) {
                Optional<User> userOptional = userDAO.findById(Long.parseLong(requestMap.get("id")));

                // Check if the user exists
                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // Update user information based on the request
                    if (requestMap.containsKey("firstName")) {
                        user.setFirstName(requestMap.get("firstName"));
                    }

                    if (requestMap.containsKey("lastName")) {
                        user.setLastName(requestMap.get("lastName"));
                    }

                    if (requestMap.containsKey("phoneNumber")) {
                        user.setPhoneNumber(requestMap.get("phoneNumber"));
                    }

                    if (requestMap.containsKey("dob")) {
                        // Assuming 'dob' is a String in the format "yyyy-MM-dd"
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date dob = dateFormat.parse(requestMap.get("dob"));
                        user.setDob(dob);
                    }

                    if (requestMap.containsKey("email")) {
                        user.setEmail(requestMap.get("email"));
                    }

                    if (requestMap.containsKey("password")) {
                        // You might want to hash the password before setting it
                        user.setPassword(requestMap.get("password"));
                    }

                    // Add more fields to update as needed

                    // Save the updated user
                    userDAO.save(user);

                    return ResponseEntity.ok("User Updated Successfully");
                } else {
                    return ResponseEntity.status(401).body("User not found");
                }
            } else {
                // Handle the case where 'id' is null or not present
                return ResponseEntity.badRequest().body("Invalid or missing 'id' parameter");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Something went wrong");
        }
    }




    @Override
    public ResponseEntity<String> checkToken() {
        return JPLearningUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            //check user null or not
            if (!user.equals(null)) {
                //check old password
                if (passwordEncoder.matches(requestMap.get("password"), user.getPassword())) {
                    user.setPassword(requestMap.get("newPassword"));
                    userDAO.save(user);
                    return JPLearningUtils.getResponseEntity("Password Update Successfully", HttpStatus.OK);
                } else {
                    return JPLearningUtils.getResponseEntity("Incorrect old password", HttpStatus.BAD_GATEWAY);
                }
            } else {
                return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
        return null;
    }


    private User getUserFromMap(Map<String, String> requestMap) {
        return User.builder()
                .firstName(requestMap.get("firstName"))
                .lastName(requestMap.get("lastName"))
                .phoneNumber(requestMap.get("phoneNumber"))
                .dob(parseDate(requestMap.get("dob")))
                .email(requestMap.get("email"))
                .password(passwordEncoder.encode(requestMap.get("password")))
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    private Date parseDate(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dob);
        } catch (ParseException e) {
            // Xử lý ngoại lệ (có thể in log, thông báo lỗi, trả về giá trị mặc định, ...)
            e.printStackTrace();
            return null;  // hoặc throw new RuntimeException("Không thể chuyển đổi ngày tháng", e);
        }
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("email") && requestMap.containsKey("password")
                && requestMap.containsKey("phoneNumber")) {
            return true;
        } else {
            return false;
        }
    }
}
