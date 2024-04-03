package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Bill;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.Lesson;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.BillDAO;
import com.in.jplearning.repositories.LessonDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.LessonService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final S3Client s3Client;
    private final LessonDAO lessonDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    private final BillDAO billDao;

    @Override
    public ResponseEntity<?> getLesson(Long lessonID) {
        try{
            Bill currentBill = billDao.getUserLatestBill(jwtAuthFilter.getCurrentUser(), PageRequest.of(0,1)).get(0);
            if(currentBill.getExpireAt().isBefore(LocalDateTime.now())){
                return JPLearningUtils.getResponseEntity("Tài khoản của bạn đã hết hạn",HttpStatus.PAYMENT_REQUIRED);
            }
            return new ResponseEntity<>(lessonDAO.findById(lessonID).get(), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> findCourseByLessonID(Long lessonID) {
        try{
            Optional<Course> courseOptional = lessonDAO.findCourseByLessonId(lessonID);
            if(courseOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Không tồn tại", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(courseOptional.get(), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
