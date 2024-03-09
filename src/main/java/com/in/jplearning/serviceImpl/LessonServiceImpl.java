package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.model.Lesson;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.LessonDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.LessonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final S3Client s3Client;
    private final LessonDAO lessonDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<Lesson> getLesson(Long lessonID) {
        try{
            return new ResponseEntity<>(lessonDAO.findById(lessonID).get(), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Lesson(), HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
