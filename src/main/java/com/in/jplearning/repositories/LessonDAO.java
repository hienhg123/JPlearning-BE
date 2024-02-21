package com.in.jplearning.repositories;

import com.in.jplearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LessonDAO extends JpaRepository<Lesson,Long> {

    @Query("select l from Lesson l where l.chapter.chapterID =?1 order by l.lessonOrder ASC ")
    List<Lesson> getLessonByLessonOrderAndChapterID(Long chapterID);

    @Query("select l from Lesson l where l.chapter.chapterID =?1 and l.lessonOrder = ?2")
    Lesson getLesson(Long chapterID, Integer lessonOrder);
}
