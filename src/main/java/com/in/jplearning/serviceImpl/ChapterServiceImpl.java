package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.ChapterDAO;
import com.in.jplearning.repositories.UserChapterProgressDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.repositories.UserLessonProgressDAO;
import com.in.jplearning.service.ChapterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
@AllArgsConstructor
public class ChapterServiceImpl implements ChapterService {

    private final ChapterDAO chapterDAO;
    private final UserLessonProgressDAO userLessonProgressDAO;
    private final UserChapterProgressDAO userChapterProgressDAO;
    private final UserDAO userDAO;
    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<Chapter> getChapterLesson(Long chapterID) {
        try{
            return new ResponseEntity<>(chapterDAO.getChapterLessonByOrder(chapterID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Chapter(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public double calculateCourseProgressByUser(User user, Course course) {
        List<Chapter> chapters = course.getChapterList();
        int totalChapters = chapters.size();
        int finishedChapters = 0;

        List<UserChapterProgress> userChapterProgressList = userChapterProgressDAO.findByUser(user);

        for (Chapter chapter : chapters) {
            for (UserChapterProgress progress : userChapterProgressList) {
                if (progress.getChapter().equals(chapter) && progress.getIsFinished()) {
                    finishedChapters++;
                    break; // No need to check other progress entries for this chapter
                }
            }
        }

        if (totalChapters == 0) {
            return 0.0; // Avoid division by zero
        }

        return ((double) finishedChapters / totalChapters) * 100;
    }




}

