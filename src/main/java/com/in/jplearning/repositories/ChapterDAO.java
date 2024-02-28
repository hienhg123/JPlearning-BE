package com.in.jplearning.repositories;

import com.in.jplearning.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChapterDAO extends JpaRepository<Chapter, Long> {


    @Query("select c from Chapter c left join c.lessonList a where c.chapterID = ?1 order by a.lessonOrder ASC")
    Chapter getChapterLessonByOrder(Long chapterID);
}
