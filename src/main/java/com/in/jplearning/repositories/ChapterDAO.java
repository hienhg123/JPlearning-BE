package com.in.jplearning.repositories;

import com.in.jplearning.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChapterDAO extends JpaRepository<Chapter, Long> {

    @Query("select c from Chapter c where c.course.courseID =?1")
    List<Chapter> getAllChapterByCourseID(Long courseID);
}
