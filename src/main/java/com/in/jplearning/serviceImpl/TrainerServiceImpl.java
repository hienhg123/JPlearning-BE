package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.NotificationType;
import com.in.jplearning.enums.Status;
import com.in.jplearning.enums.VerificationType;
import com.in.jplearning.model.Notification;
import com.in.jplearning.model.Trainer;
import com.in.jplearning.model.User;
import com.in.jplearning.model.VerifyRequest;
import com.in.jplearning.repositories.NotificationDAO;
import com.in.jplearning.repositories.TrainerDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.repositories.VerifyRequestDAO;
import com.in.jplearning.service.TrainerService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private final TrainerDAO trainerDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    private final String cloudFront = "https://d3gzlpw1zpqj23.cloudfront.net";

    private final String bucketName = "trainerverfiy";

    private final VerifyRequestDAO verifyRequestDAO;

    private final NotificationDAO notificationDAO;
    private final S3AsyncClient s3AsyncClient;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    @Transactional
    public ResponseEntity<String> registerAsTrainer(List<MultipartFile> pictureFiles, Map<String, String> requestMap) {
        try {
            String uuid = UUID.randomUUID().toString();
            String verifyUrl = "";
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());


            //get current user
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            User user = userOptional.get();
            List<VerifyRequest> verifyRequestList = verifyRequestDAO.getByUserId(userOptional.get().getUserID());
            //check if user have sent the request
            if(!verifyRequestList.isEmpty()){
                VerifyRequest verifyRequest = verifyRequestList.get(verifyRequestList.size()-1);
                if(verifyRequest.getStatus().equals(Status.PENDING)){
                    return JPLearningUtils.getResponseEntity("Yêu cầu của bạn đang chờ được xử lí", HttpStatus.BAD_REQUEST);
                }
            }
            //check if user is already trainer
            if (checkExist(user)) {
                return JPLearningUtils.getResponseEntity("Bạn đã là trainer", HttpStatus.BAD_REQUEST);
            }
            //save user in trainer table
            Trainer trainer = Trainer.builder()
                    .isVerify(false)
                    .user(user)
                    .currentJob(requestMap.get("currentJob"))
                    .jlptLevel(JLPTLevel.valueOf(requestMap.get("jlptLevel")))
                    .fullName(requestMap.get("fullName"))
                    .dob(parseDate(requestMap.get("dob")))
                    .build();
            //get the certificate picture and push to aws s3
            if (pictureFiles == null || pictureFiles.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Không tìm thấy ảnh", HttpStatus.BAD_REQUEST);
            }
            //check img content
            for (MultipartFile multipartFile : pictureFiles) {
                verifyUrl += cloudFront + "/" + uuid + multipartFile.getOriginalFilename() + ", ";
                if (!(multipartFile.getContentType().endsWith("png") || multipartFile.getContentType().endsWith("jpeg"))) {
                    return JPLearningUtils.getResponseEntity("Định dạng ảnh chưa đúng", HttpStatus.BAD_REQUEST);
                }
                if(multipartFile.getSize() > 500 * 1024 * 1024){
                    return JPLearningUtils.getResponseEntity(
                            multipartFile.getOriginalFilename() + "quá dung lượng cho phép, tối đa 500MB", HttpStatus.BAD_REQUEST);
                }
            }
            //save into verify request table
            VerifyRequest verifyRequest = VerifyRequest.builder()
                    .verificationType(VerificationType.JLPT_CERTIFICATE_VERIFICATION)
                    .status(Status.PENDING)
                    .url(verifyUrl)
                    .trainer(trainer)
                    .requestTimestamp(LocalDateTime.now())
                    .build();
            trainerDAO.save(trainer);
            verifyRequestDAO.save(verifyRequest);
            uploadToS3(pictureFiles, uuid);
            return JPLearningUtils.getResponseEntity("Đăng kí thành công, yêu cầu của bạn sẽ được xử lí trong vòng 24 giờ", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            log.info(String.valueOf(jwtAuthFilter.isManager()));

            //check if user is manager
            if (jwtAuthFilter.isManager()) {
                Trainer trainer = trainerDAO.findById(Long.parseLong(requestMap.get("trainerID"))).get();
                //check if user empty
                if (trainer != null) {
                    trainerDAO.updateStatus(Boolean.parseBoolean(requestMap.get("isVerify")), trainer.getTrainerID());
                    verifyRequestDAO.updateStatus(Status.valueOf(requestMap.get("status")), Long.parseLong(requestMap.get("requestID")));
                    //check if approve or reject
                    if (Status.valueOf(requestMap.get("status")).equals(Status.APPROVED)) {
                        notificationDAO.save(getNotificationFromMap(trainer.getUser().getUserID(), "Tài khoản của bạn đã được xác thực"));
                    } else {
                        notificationDAO.save(getNotificationFromMap(trainer.getUser().getUserID(), "Tài khoản của bạn không được chấp nhận"));
                    }
                    return JPLearningUtils.getResponseEntity("Thay đổi thành công", HttpStatus.OK);
                }
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
            }
            return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getAllTrainer(int pageNumber, int pageSize) {
        try {
            //check if manager
            if (jwtAuthFilter.isManager()) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("trainerID").descending());
                return new ResponseEntity<>(trainerDAO.getAllTrainer(pageable), HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateTrainerStatus(Map<String, String> requestMap) {
        try {
            Optional<Trainer> optionalTrainer = trainerDAO.findById(Long.parseLong(requestMap.get("trainerID")));
            //check if admin
            if (!jwtAuthFilter.isManager()) {
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            if (optionalTrainer.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Không tìm thấy", HttpStatus.NOT_FOUND);
            }
            optionalTrainer.get().setVerify(Boolean.parseBoolean(requestMap.get("isVerify")));
            trainerDAO.save(optionalTrainer.get());
            return JPLearningUtils.getResponseEntity("Cập nhật thành công", HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> checkTrainer() {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            Trainer trainerOptional = trainerDAO.getByUserId(userOptional.get().getUserID());
            if(trainerOptional == null){
                return JPLearningUtils.getResponseEntity("false", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("true", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> checkOtherTrainer(Long userID) {
        try{
            Optional<User> userOptional = userDAO.findById(userID);
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Không tồn tại", HttpStatus.UNAUTHORIZED);
            }
            Trainer trainerOptional = trainerDAO.getByUserId(userID);
            if(trainerOptional == null){
                return JPLearningUtils.getResponseEntity("false", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("true", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Notification getNotificationFromMap(Long userID, String content) {
        User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
        User sender = User.builder()
                .userID(user.getUserID())
                .build();
        User receiver = User.builder()
                .userID(userID)
                .build();
        Notification notification = Notification.builder()
                .notificationType(NotificationType.VERIFY)
                .receiver(receiver)
                .content(content)
                .isRead(false)
                .sender(sender)
                .createdTime(LocalDateTime.now())
                .build();
        return notification;

    }

    private boolean checkExist(User user) {
        if (trainerDAO.getByUserId(user.getUserID()) != null) {
            return true;
        }
        return false;
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

    private void uploadToS3(List<MultipartFile> files, String uuid) throws IOException {
        List<CompletableFuture<PutObjectResponse>> uploadFutures = files.stream()
                .map(file -> {
                    try {
                        return uploadToS3Async(file, uuid);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private CompletableFuture<PutObjectResponse> uploadToS3Async(MultipartFile file, String uuid) throws IOException {
        String key = uuid + file.getOriginalFilename();
        return s3AsyncClient.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                AsyncRequestBody.fromInputStream(file.getInputStream(), file.getSize(), executorService));
    }
}
