package com.in.jplearning.serviceImpl;

import com.in.jplearning.model.Chapter;
import com.in.jplearning.repositories.ChapterDAO;
import com.in.jplearning.service.ChapterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@AllArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterDAO chapterDAO;

    @Override
    public ResponseEntity<Chapter> getChapterLesson(Long chapterID) {
        try{
            return new ResponseEntity<>(chapterDAO.getChapterLessonByOrder(chapterID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Chapter(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
