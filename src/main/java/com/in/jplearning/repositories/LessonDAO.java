package com.in.jplearning.repositories;

import com.in.jplearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonDAO extends JpaRepository<Lesson,Long> {
}
