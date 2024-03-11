package com.in.jplearning.repositories;

import com.in.jplearning.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLessonProgressDAO extends JpaRepository<UserLessonProgress, Long> {
    List<UserLessonProgress> findByUserAndChapter(User user, Chapter chapter);
    Optional<UserLessonProgress> findByUserAndLesson(User user, Lesson lesson);

}
