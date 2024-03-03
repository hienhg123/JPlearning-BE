package com.in.jplearning.serviceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.config.JwtUtil;
import com.in.jplearning.config.S3Config;
import com.in.jplearning.constants.JPConstants;

import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.Role;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.UserService;
import com.in.jplearning.utils.EmailUtils;
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
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final S3Config s3Config;
    private final EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> register(Map<String, String> requestMap) {
        log.info("Inside sign up {}", requestMap);
        try {
            // Check if email and password are provided in the registration request
            if (validateSignUpMap(requestMap)) {
                // Check if email already exists
                if (userDAO.findByEmail(requestMap.get("email")).isPresent()) {
                    return JPLearningUtils.getResponseEntity("Email đã tồn tại", HttpStatus.BAD_REQUEST);
                }
                userDAO.save(getUserFromMap(requestMap));
                return JPLearningUtils.getResponseEntity("Đăng kí thành công", HttpStatus.OK);
            } else {
                return JPLearningUtils.getResponseEntity(JPConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
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
        //check if user exist
        if(userDAO.findByEmail(requestMap.get("email")).isEmpty()){
            return new ResponseEntity<String>("Tài khoản không tồn tại", HttpStatus.BAD_REQUEST);
        }
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
                    return new ResponseEntity<String>("Tài khoản đã bị khóa", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Mật khẩu không đúng" + "\"}"
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

                    if (requestMap.containsKey("level")) {
                        String levelString = requestMap.get("level");
                        JLPTLevel level = JLPTLevel.valueOf(levelString); // Assuming level names match the enum values
                        user.setLevel(level);
                    }

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
            // check user null or not
            if (user != null) {
                // check old password
                if (passwordEncoder.matches(requestMap.get("password"), user.getPassword())) {
                    String newPassword = requestMap.get("newPassword");
                    String encodedNewPassword = passwordEncoder.encode(newPassword);

                    // Log statements for debugging
                    System.out.println("Old Password: " + requestMap.get("password"));
                    System.out.println("Stored Password: " + user.getPassword());
                    System.out.println("New Password: " + newPassword);
                    System.out.println("Encoded New Password: " + encodedNewPassword);

                    user.setPassword(encodedNewPassword);
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
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Override
    public ResponseEntity<Map<String,String>> forgetPassword(Map<String, String> requestMap) {
        Map<String, String> response = new HashMap<>();
        response.put("message","Something went wrong");
        try{
            User user = userDAO.findByEmail(requestMap.get("email")).get();
            //check if user exist
            if(!user.equals(null)){
                response.put("message","Check your email for verification code");
                response.put("email", user.getEmail());
                //create verify code
                String otp = generateVerifyCode();
                //set expire time
                long expirationTime = System.currentTimeMillis() + 10 * 60 * 1000;
                //send verify code to email
//                emailUtils.sendVerifyCode(user.getEmail(),"Verification Code",code);
                response.put("expirationTime",String.valueOf(expirationTime));
                response.put("generatedOtp",otp);
            }
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> validateOtp(Map<String, String> requestMap) {
        try{
            //get otp that user enter and generated otp
            String otp = requestMap.get("otp");
            String generatedOtp = requestMap.get("generatedOtp");
            //check credentials
            if(!otp.isEmpty()){
                //validate otp
                if(otp.equals(generatedOtp)){
                    return JPLearningUtils.getResponseEntity("",HttpStatus.OK);
                } else {
                    return JPLearningUtils.getResponseEntity("Sai mã OTP",HttpStatus.OK);
                }
            } else {
                return JPLearningUtils.getResponseEntity(JPConstants.INVALID_DATA,HttpStatus.BAD_REQUEST);
            }


        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        try{
            //get user
            User user = userDAO.findByEmail(requestMap.get("email")).get();
            //update new password
            user.setPassword(passwordEncoder.encode(requestMap.get("password")));
            userDAO.save(user);
            return JPLearningUtils.getResponseEntity("Reset password thành công",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> getUserProfile() {
        try {
            // Get the currently logged-in user's email (you might need to adjust this based on your authentication mechanism)
            String userEmail = jwtAuthFilter.getCurrentUser();

            // Retrieve the user by email
            Optional<User> userOptional = userDAO.findByEmail(userEmail);

            // Check if the user exists
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Create a Map with desired fields
                Map<String, Object> userProfile = new HashMap<>();
                userProfile.put("firstName", user.getFirstName());
                userProfile.put("lastName", user.getLastName());
                userProfile.put("phoneNumber", user.getPhoneNumber());

                // Format date of birth
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDob = dateFormat.format(user.getDob());
                userProfile.put("dob", formattedDob);

                userProfile.put("level", user.getLevel());
                userProfile.put("gender", user.getGender());

                // Convert Map to JSON and return it
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonProfile = objectMapper.writeValueAsString(userProfile);

                return ResponseEntity.ok(jsonProfile);
            } else {
                return ResponseEntity.badRequest().body(JPConstants.USER_NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(JPConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public ResponseEntity<String> updateProfile(Long userId, MultipartFile userPicture, Map<String, String> requestMap) {
        try {
            // Retrieve the user by ID
            Optional<User> userOptional = userDAO.findById(userId);

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
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date dob = dateFormat.parse(requestMap.get("dob"));
                    user.setDob(dob);
                }

                if (requestMap.containsKey("level")) {
                    String levelString = requestMap.get("level");
                    JLPTLevel level = JLPTLevel.valueOf(levelString);
                    user.setLevel(level);
                }

                if (requestMap.containsKey("gender")) {
                    user.setGender(requestMap.get("gender"));
                }

                // Check if userPicture is not null before interacting with AWS S3
                if (userPicture != null && !userPicture.isEmpty()) {
                    // Save user picture to AWS S3 and get the URL
                    String userPictureUrl = saveUserPictureToS3(userId, userPicture); // Use userId here
                    user.setUserPicture(userPictureUrl);
                }

                // Save the updated user
                userDAO.save(user);

                return ResponseEntity.ok("Profile Updated Successfully");
            } else {
                return ResponseEntity.badRequest().body("User not found with ID: " + userId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }

    private String saveUserPictureToS3(Long userId, MultipartFile userPicture) throws IOException {
        try {
            // Retrieve the user by ID
            Optional<User> userOptional = userDAO.findById(userId);

            // Check if the user exists
            if (userOptional.isPresent()) {
                User user = userOptional.get();


                String email = user.getEmail();
                String phoneNumber = user.getPhoneNumber();

                // Generate a unique key for the picture in S3, incorporating email and phoneNumber
                String key = "user-pictures/" + email + "_" + "/" + userPicture.getOriginalFilename();

                // Get S3 client bean from S3Config
                S3Client s3Client = s3Config.s3Client();

                // Upload the picture to S3
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket("jplearning-lesson")
                        .key(key)
                        .build(), RequestBody.fromByteBuffer(ByteBuffer.wrap(userPicture.getBytes())));

                // Return the URL of the uploaded picture
                return "https://jplearning-lesson.s3.amazonaws.com/" + key;
            } else {
                throw new RuntimeException("User not found with ID: " + userId);
            }
        } catch (Exception ex) {
            throw new IOException("Error saving user picture to S3", ex);
        }
    }



    private String generateVerifyCode() {
        String code ="";
         Random random = new Random();
         //loop go generate 6 number
        for (int i =0;i < 6 ; i++){
            code += String.valueOf(random.nextInt(10));
        }
        return code;
    }


    private User getUserFromMap(Map<String, String> requestMap) {

        return User.builder()
                .firstName(requestMap.get("firstName"))
                .lastName(requestMap.get("lastName"))
                .email(requestMap.get("email"))
                .password(passwordEncoder.encode(requestMap.get("password")))
                .role(Role.USER)
//              .level(JLPTLevel.None)
                .isActive(true)
                .build();
    }


    private Date parseDate(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dob);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("email") && requestMap.containsKey("password")) {
            return true;
        } else {
            return false;
        }
    }


}


