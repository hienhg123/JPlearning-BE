package com.in.jplearning.repo;

import com.in.jplearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDAO extends JpaRepository<Course,Long> {
}
