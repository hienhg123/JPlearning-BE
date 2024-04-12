package com.in.jplearning.repositories;

import com.in.jplearning.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLessonProgressDAO extends JpaRepository<UserLessonProgress, Long> {

    @Query("select p from UserLessonProgress p where p.lesson.lessonID=?1 and p.user =?2")
    Optional<UserLessonProgress> getByLessonId(Long lessonID, User user);

    @Query("select count(p) from UserLessonProgress p where p.user =?1 and p.lesson.chapter =?2 and p.isFinished = true")
    long countFinishedByUserAndChapter(User user, Chapter chapter);
}
