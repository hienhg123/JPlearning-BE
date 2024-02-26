package com.in.jplearning.serviceImpl;

import com.in.jplearning.model.Lesson;
import com.in.jplearning.repositories.LessonDAO;
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
    @Override
    public ResponseEntity<List<Lesson>> getLessonByLessonOrderAndChapterID(Long chapterID) {
        try{
            return new ResponseEntity<>(lessonDAO.getLessonByLessonOrderAndChapterID(chapterID),HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Lesson> getLesson(Long chapterID, Integer lessonOrder) {
        try{
            //get video
            Lesson lesson = lessonDAO.getLesson(chapterID,lessonOrder);
            log.info("hehe");
            return new ResponseEntity<>(lessonDAO.getLesson(chapterID,lessonOrder), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Lesson(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
