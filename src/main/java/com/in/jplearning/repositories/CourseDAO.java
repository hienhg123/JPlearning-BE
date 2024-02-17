package com.in.jplearning.repositories;

import com.in.jplearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseDAO extends JpaRepository<Course,Long> {

    Optional<Course> findByCourseName(String courseName );
    List<Course> findAll();
}
