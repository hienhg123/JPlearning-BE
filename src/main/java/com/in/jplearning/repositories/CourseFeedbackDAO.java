package com.in.jplearning.repositories;

import com.in.jplearning.model.Course;
import com.in.jplearning.model.CourseFeedBack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseFeedbackDAO extends JpaRepository<CourseFeedBack,Long> {
    @Query("SELECT AVG(cf.rating) FROM CourseFeedBack cf WHERE cf.course = :course")
    Double calculateAverageRating(@Param("course") Course course);
}
