package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.VerificationType;
import com.in.jplearning.model.Trainer;
import com.in.jplearning.model.User;
import com.in.jplearning.model.VerifyRequest;
import com.in.jplearning.repositories.TrainerDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.repositories.VerifyRequestDAO;
import com.in.jplearning.service.TrainerService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private final TrainerDAO trainerDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    private final String cloudFront = "https://d3gzlpw1zpqj23.cloudfront.net";

    private final String bucketName = "trainerverfiy";

    private final S3Client s3Client;

    private final VerifyRequestDAO verifyRequestDAO;
    @Override
    @Transactional
    public ResponseEntity<String> registerAsTrainer(MultipartFile pictureFiles) {
        try{
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            //get current user
            if(userOptional.isEmpty()){
               return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            User user = userOptional.get();
            //check if user is already trainer
            if(checkExist(user)){
                return JPLearningUtils.getResponseEntity("Bạn đã là trainer", HttpStatus.BAD_REQUEST);
            }
            //save user in trainer table
            Trainer trainer = Trainer.builder()
                    .isVerify(false)
                    .user(user)
                    .build();
            //get the certificate picture and push to aws s3
            if(pictureFiles == null || pictureFiles.isEmpty()){
                return JPLearningUtils.getResponseEntity("Không tìm thấy ảnh", HttpStatus.BAD_REQUEST);
            }
            //check img content
            if(!(pictureFiles.getContentType().endsWith("png") || pictureFiles.getContentType().endsWith("jpeg"))){
                return JPLearningUtils.getResponseEntity("Định dạng ảnh chưa đúng", HttpStatus.BAD_REQUEST);
            }
            PutObjectRequest request = PutObjectRequest.builder()
                    .key(pictureFiles.getOriginalFilename())
                    .bucket(bucketName)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(pictureFiles.getInputStream(),pictureFiles.getSize()));
            trainerDAO.save(trainer);
            //save into verify request table
            String verifyUrl = cloudFront + "/" + pictureFiles.getOriginalFilename();
            Date currentDate = getDate();
            VerifyRequest verifyRequest = VerifyRequest.builder()
                    .verificationType(VerificationType.JLPT_CERTIFICATE_VERIFICATION)
                    .approved(false)
                    .url(verifyUrl)
                    .trainee(trainer)
                    .requestTimestamp(currentDate)
                    .build();
            verifyRequestDAO.save(verifyRequest);
            return JPLearningUtils.getResponseEntity("Đăng kí thành công", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean checkExist(User user) {
        if(trainerDAO.getByUserId(user.getUserID()) != null){
            return true;
        }
        return false;
    }
    private Date getDate() {
        LocalDate currentDate = LocalDate.now();
        return Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    }
}
