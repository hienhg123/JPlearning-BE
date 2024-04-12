package com.in.jplearning.repositories;

import com.in.jplearning.model.Chapter;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LessonDAO extends JpaRepository<Lesson,Long> {

    @Query("SELECT l.chapter.course FROM Lesson l WHERE l.lessonID = ?1")
    Optional<Course> findCourseByLessonId(Long lessonId);

    @Query("select l.chapter from Lesson l where l.lessonID =?1")
    Chapter getChapterByLessonId(long lessonID);
}
