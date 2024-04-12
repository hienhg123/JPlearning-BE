package com.in.jplearning.repositories;

import com.in.jplearning.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface UserChapterProgressDAO extends JpaRepository<UserChapterProgress,Long> {
    @Query("SELECT ucp FROM UserChapterProgress ucp WHERE ucp.chapter = :chapter")
    List<UserChapterProgress> findByChapter(Chapter chapter);

    @Query("select ucp from UserChapterProgress ucp where ucp.user =?1 and ucp.chapter =?2")
    Optional<UserChapterProgress> getByUserAndExercise(User user, Chapter chapter);
}
