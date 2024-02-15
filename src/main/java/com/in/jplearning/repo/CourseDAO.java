package com.in.jplearning.repo;

import com.in.jplearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface CourseDAO extends JpaRepository<Course,Long> {

    Optional<Course> findByCourseName(String courseName );
    List<Course> findAll();
}
