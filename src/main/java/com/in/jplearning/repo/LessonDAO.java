package com.in.jplearning.repo;

import com.in.jplearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonDAO extends JpaRepository<Lesson,Long> {
}
