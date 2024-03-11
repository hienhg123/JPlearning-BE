package com.in.jplearning.serviceImpl;

import com.in.jplearning.model.*;
import com.in.jplearning.repositories.ChapterDAO;
import com.in.jplearning.repositories.UserChapterProgressDAO;
import com.in.jplearning.repositories.UserLessonProgressDAO;
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
    private final UserLessonProgressDAO userLessonProgressDAO;
    private final UserChapterProgressDAO userChapterProgressDAO;

    @Override
    public ResponseEntity<Chapter> getChapterLesson(Long chapterID) {
        try{
            return new ResponseEntity<>(chapterDAO.getChapterLessonByOrder(chapterID), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Chapter(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public void updateLessonProgress(User_Exercise userExercise) {
        if (userExercise.getMark() > 20) {
            UserLessonProgress lessonProgress = userLessonProgressDAO
                    .findByUserAndLesson(userExercise.getUser(), userExercise.getExercises().getLesson())
                    .orElse(new UserLessonProgress());

            lessonProgress.setIsFinished(true);
            lessonProgress.setUser(userExercise.getUser());
            lessonProgress.setLesson(userExercise.getExercises().getLesson());

            userLessonProgressDAO.save(lessonProgress);
        }
    }

    public void updateChapterProgress(User user, Chapter chapter) {
        List<UserLessonProgress> lessonProgressList = userLessonProgressDAO.findByUserAndChapter(user, chapter);

        // Check if all UserLessonProgress instances are finished
        boolean allFinished = lessonProgressList.stream()
                .allMatch(UserLessonProgress::getIsFinished);

        if (allFinished) {
            UserChapterProgress chapterProgress = userChapterProgressDAO
                    .findByUserAndChapter(user, chapter)
                    .orElse(new UserChapterProgress());

            chapterProgress.setIsFinished(true);
            chapterProgress.setUser(user);
            chapterProgress.setChapter(chapter);

            userChapterProgressDAO.save(chapterProgress);
        }
    }






}

