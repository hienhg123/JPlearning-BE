package com.in.jplearning.serviceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.config.JwtUtil;
import com.in.jplearning.config.S3Config;
import com.in.jplearning.constants.JPConstants;

import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.Role;
import com.in.jplearning.model.Bill;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.BillDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.UserService;
import com.in.jplearning.utils.EmailUtils;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDateTime;
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

    private final BillDAO billDAO;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final String cloudFront = "https://ddzgswoq4gt6i.cloudfront.net/";
    @Override
    public ResponseEntity<String> register(Map<String, String> requestMap) {
        log.info("Inside sign up {}", requestMap);
        try {
            // Check if email and password are provided in the registration request
            if (validateSignUpMap(requestMap)) {
                // Check if email already exists
                if (userDAO.findByEmail(requestMap.get("email")).isPresent()) {
                    return JPLearningUtils.getResponseEntity("Email đã được sử dụng.Xin sử dụng email khác ", HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<?> checkPremium() {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(isPremiumExpire(userOptional.get()), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getThatUserProfile(Long userID) {
        try{
            Optional<User> userOptional = userDAO.findById(userID);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Create a map to hold user profile information
                Map<String, Object> userProfile = new HashMap<>();
                userProfile.put("firstName", user.getFirstName());
                userProfile.put("lastName", user.getLastName());
                userProfile.put("phoneNumber", user.getPhoneNumber());

                // Check if dob is not null before formatting
                if (user.getDob() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dobFormatted = sdf.format(user.getDob());
                    userProfile.put("dob", dobFormatted);
                } else {
                    userProfile.put("dob", null); // Or handle it as needed
                }

                // Check if level is not null before adding
                if (user.getLevel() != null) {
                    userProfile.put("level", user.getLevel());
                } else {
                    userProfile.put("level", null); // Or handle it as needed
                }

                // Check if gender is not null before adding
                if (user.getGender() != null) {
                    userProfile.put("gender", user.getGender());
                } else {
                    userProfile.put("gender", null); // Or handle it as needed
                }

                // Check if userPicture is not null before adding
                if (user.getUserPicture() != null) {
                    userProfile.put("userPicture", user.getUserPicture());
                } else {
                    userProfile.put("userPicture", null); // Or handle it as needed
                }

                return ResponseEntity.ok(userProfile);
            } else {
                // User not found with the given email
                return ResponseEntity.notFound().build();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> checkThatUserPremium(Long userID) {
        try{
            Optional<User> userOptional = userDAO.findById(userID);
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(isPremiumExpire(userOptional.get()), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        Optional<User> userOptional = userDAO.findByEmail(requestMap.get("email"));
        //check if user exist
        if(userOptional.isEmpty()){
            return JPLearningUtils.getResponseEntity("Tài khoản không tồn tại", HttpStatus.BAD_REQUEST);
        }
        User user = userOptional.get();
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
                    return JPLearningUtils.getResponseEntity("Tài khoản đã bị khóa", HttpStatus.BAD_REQUEST);
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
                    return JPLearningUtils.getResponseEntity("Xin hãy đăng nhập", HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        try {
            // Get the current user's email from JWT
            String userEmail = jwtAuthFilter.getCurrentUser();

            // Retrieve the user by email
            Optional<User> userOptional = userDAO.findByEmail(userEmail);

            // Check if the user exists
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Create a map to hold user profile information
                Map<String, Object> userProfile = new HashMap<>();
                userProfile.put("firstName", user.getFirstName());
                userProfile.put("lastName", user.getLastName());
                userProfile.put("phoneNumber", user.getPhoneNumber());

                // Check if dob is not null before formatting
                if (user.getDob() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dobFormatted = sdf.format(user.getDob());
                    userProfile.put("dob", dobFormatted);
                } else {
                    userProfile.put("dob", null); // Or handle it as needed
                }

                // Check if level is not null before adding
                if (user.getLevel() != null) {
                    userProfile.put("level", user.getLevel());
                } else {
                    userProfile.put("level", null); // Or handle it as needed
                }

                // Check if gender is not null before adding
                if (user.getGender() != null) {
                    userProfile.put("gender", user.getGender());
                } else {
                    userProfile.put("gender", null); // Or handle it as needed
                }

                // Check if userPicture is not null before adding
                if (user.getUserPicture() != null) {
                    userProfile.put("userPicture", user.getUserPicture());
                } else {
                    userProfile.put("userPicture", null); // Or handle it as needed
                }

                return ResponseEntity.ok(userProfile);
            } else {
                // User not found with the given email
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @Override
    public ResponseEntity<String> updateProfile(MultipartFile userPicture, Map<String, String> requestMap) {
        try {
            // Retrieve the user by email
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();
            //check the request
            if (requestMap.containsKey("firstName")) {
                String firstName = requestMap.get("firstName").trim(); // Trim to remove leading and trailing spaces
                if (firstName == null || firstName.isEmpty()) {
                    return JPLearningUtils.getResponseEntity("Không được để trống", HttpStatus.BAD_REQUEST);
                } else if (firstName.length() < 2) {
                    return JPLearningUtils.getResponseEntity("Độ dài tối thiểu là 2 ký tự", HttpStatus.BAD_REQUEST);
                } else if (firstName.length() > 20) {
                    return JPLearningUtils.getResponseEntity("Độ dài tối đa là 20 ký tự", HttpStatus.BAD_REQUEST);
                } else if (!firstName.matches("[\\p{L}\\s]{2,20}")) {
                    return JPLearningUtils.getResponseEntity("Không được có ký tự đặc biệt trong tên", HttpStatus.BAD_REQUEST);
                }
                user.setFirstName(firstName);
            }

            if (requestMap.containsKey("lastName")) {
                String lastName = requestMap.get("lastName").trim(); // Trim to remove leading and trailing spaces
                if (lastName == null || lastName.isEmpty()) {
                    return JPLearningUtils.getResponseEntity("Không được để trống", HttpStatus.BAD_REQUEST);
                } else if (lastName.length() < 2) {
                    return JPLearningUtils.getResponseEntity("Độ dài tối thiểu là 2 ký tự", HttpStatus.BAD_REQUEST);
                } else if (lastName.length() > 20) {
                    return JPLearningUtils.getResponseEntity("Độ dài tối đa là 20 ký tự", HttpStatus.BAD_REQUEST);
                } else if (!lastName.matches("[\\p{L}\\s]{2,20}")) {
                    return JPLearningUtils.getResponseEntity("Không được có ký tự đặc biệt trong tên", HttpStatus.BAD_REQUEST);
                }
                user.setLastName(lastName);
            }

            if (requestMap.containsKey("phoneNumber")) {
                String phoneNumber = requestMap.get("phoneNumber").trim();
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    return JPLearningUtils.getResponseEntity("Không được để trống", HttpStatus.BAD_REQUEST);
                } else if (!phoneNumber.matches("\\d{10,11}")) {
                    return JPLearningUtils.getResponseEntity("Số điện thoại phải có 10 hoặc 11 chữ số", HttpStatus.BAD_REQUEST);
                }
                user.setPhoneNumber(phoneNumber);
            }

            if (requestMap.containsKey("dob")) {
                user.setDob(parseDate(requestMap.get("dob")));
            }

            if (!requestMap.get("level").isEmpty()) {
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
            userDAO.save(user);
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
    private boolean isPremiumExpire(User user) {
        //get user premium
        List<Bill> bills = billDAO.getUserLatestBill(user.getEmail(), PageRequest.of(0, 1));
        if(bills.isEmpty()){
            return false;
        }
        Bill bill = bills.get(0);
        //check if bill is exist
        if (bill == null) {
            return false;
        }
        //check if bill is expire or not
        return bill.getExpireAt().isAfter(LocalDateTime.now());
    }


}


