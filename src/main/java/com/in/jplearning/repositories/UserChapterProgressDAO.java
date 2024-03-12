package com.in.jplearning.repositories;

import com.in.jplearning.model.Chapter;
import com.in.jplearning.model.User;
import com.in.jplearning.model.UserChapterProgress;
import com.in.jplearning.model.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserChapterProgressDAO extends JpaRepository<UserChapterProgress,Long> {
    List<UserChapterProgress> findByUser(User user);

}
