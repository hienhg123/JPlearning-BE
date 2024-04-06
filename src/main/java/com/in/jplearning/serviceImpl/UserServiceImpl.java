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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final String cloudFront = "https://ddzgswoq4gt6i.cloudfront.net/";
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
    public ResponseEntity<Page<Map<String, Object>>> getAllUsers(int pageNumber, int pageSize) {
        try {
            // Check if the current user is an admin
            if (jwtAuthFilter.isAdmin()) {
                log.info("Fetching all users.");

                // Get a page of users from the database with the role "USER"
                Page<User> userPage = userDAO.findByRole(Role.USER, PageRequest.of(pageNumber, pageSize));

                // Convert the page of users to a page of user information maps
                Page<Map<String, Object>> userPageInfo = userPage.map(user -> {
                    Map<String, Object> userInfoMap = new HashMap<>();
                    userInfoMap.put("userID", user.getUserID());
                    userInfoMap.put("fullName", user.getFirstName() + " " + user.getLastName());
                    userInfoMap.put("email", user.getEmail());
                    userInfoMap.put("isActive", user.isActive());
                    // You can add more properties to the map if needed
                    return userInfoMap;
                });

                // Return the page of user information maps
                return ResponseEntity.ok(userPageInfo);
            } else {
                // User is not authorized to access this endpoint
                log.error("Unauthorized access. Current user is not an admin.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error occurred while fetching all users.", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @Override
    public ResponseEntity<String> updateUser(Map<String, String> requestMap) {
        try {
            // Check if admin
            if (jwtAuthFilter.isAdmin()) {
                Long userId = Long.parseLong(requestMap.get("id"));
                Optional<User> userOptional = userDAO.findById(userId);

                // Check if the user exists
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    boolean newStatus = Boolean.parseBoolean(requestMap.get("isActive"));

                    // Update the isActive status using the setter method
                    user.setActive(newStatus);
                    userDAO.save(user);
                    return JPLearningUtils.getResponseEntity("Thay đổi trạng thái thành công", HttpStatus.OK);
                } else {
                    return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
                }
            } else {
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (NumberFormatException ex) {
            // Handle number format exception (e.g., if the ID cannot be parsed)
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity("Invalid user ID format", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            // Handle other exceptions
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                    return JPLearningUtils.getResponseEntity("Cập nhật mật khẩu thành công", HttpStatus.OK);
                } else {
                    return JPLearningUtils.getResponseEntity("Mật khẩu cũ sai ", HttpStatus.BAD_GATEWAY);
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
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String formattedDOB = dateFormat.format(user.getDob());
                // Create a Map with desired fields
                Map<String, Object> userProfile = new HashMap<>();
                userProfile.put("firstName", user.getFirstName());
                userProfile.put("lastName", user.getLastName());
                userProfile.put("phoneNumber", user.getPhoneNumber());
                userProfile.put("level", user.getLevel());
                userProfile.put("gender", user.getGender());
                userProfile.put("dob", formattedDOB);
                userProfile.put("userPicture", user.getUserPicture());

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
    public ResponseEntity<String> updateProfile(MultipartFile userPicture, Map<String, String> requestMap) {
        try {
            // Retrieve the user by email
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Người dùng không tồn tại", HttpStatus.OK);
            }
            User user = userOptional.get();
            //check the request
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
                user.setDob(parseDate(requestMap.get("dob")));
            }

            if (requestMap.containsKey("level")) {
                user.setLevel(JLPTLevel.valueOf(requestMap.get("level")));
            }

            if (requestMap.containsKey("gender")) {
                user.setGender(requestMap.get("gender"));
            }

            // Check if userPicture is not null before interacting with AWS S3
            if (userPicture != null && !userPicture.isEmpty()) {
                // Save user picture to AWS S3 and get the URL
                if (!isValidImageFormat(userPicture.getOriginalFilename())) {
                    return JPLearningUtils.getResponseEntity("Sai định dảng ảnh vui lòng kiểu tra lại", HttpStatus.BAD_REQUEST);
                }
                String userPictureUrl = saveUserPictureToS3(user.getUserID(), userPicture); // Use userId here
                user.setUserPicture(userPictureUrl);
            }
            return JPLearningUtils.getResponseEntity("Thay đổi thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
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
                String key = "user-pictures/" + email + "/"  + userPicture.getOriginalFilename();

                // Get S3 client bean from S3Config
                S3Client s3Client = s3Config.s3Client();
                // Upload the picture to S3
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket("jplearning-user-profile")
                        .key(key)
                        .build(), RequestBody.fromByteBuffer(ByteBuffer.wrap(userPicture.getBytes())));

                // Return the URL of the uploaded picture
                return cloudFront + key;
            } else {
                throw new RuntimeException("User not found with ID: " + userId);
            }
        } catch (Exception ex) {
            throw new IOException("Error saving user picture to S3", ex);
        }
    }

    private boolean isValidImageFormat(String fileName) {
        String[] allowedFormats = {"png", "jpg", "jpeg"};

        // Get the file extension
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        // Check if the file format is allowed
        for (String format : allowedFormats) {
            if (fileExtension.equals(format)) {
                return true;
            }
        }

        return false;
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

    private Date parseDate(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dob);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    private User getUserFromMap(Map<String, String> requestMap) {

        return User.builder()
                .firstName(requestMap.get("firstName"))
                .lastName(requestMap.get("lastName"))
                .email(requestMap.get("email"))
                .password(passwordEncoder.encode(requestMap.get("password")))
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("email") && requestMap.containsKey("password")) {
            return true;
        } else {
            return false;
        }
    }


}


