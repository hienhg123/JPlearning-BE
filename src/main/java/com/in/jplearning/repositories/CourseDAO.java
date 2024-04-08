package com.in.jplearning.repositories;

import com.in.jplearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseDAO extends JpaRepository<Course,Long> {

    Optional<Course> findByCourseName(String courseName );

    @Query(value = "select c from Course c where c.courseName LIKE %?1%")
    List<Course> searchByValue(String value);

    List<Course> findAllByIsDraft(boolean isDraft);

    @Query("select c from Course c where c.createBy.userID = ?1 and c.isDraft = true")
    List<Course> getDraftCourse(Long userID);
    @Query("select c from Course c where c.createBy.userID = ?1 and c.isDraft = false")
    List<Course> getCreatedCourse(Long userID);

}
