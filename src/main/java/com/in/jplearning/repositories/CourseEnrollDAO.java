package com.in.jplearning.repositories;

import com.in.jplearning.model.Course;
import com.in.jplearning.model.CourseEnroll;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollDAO extends JpaRepository<CourseEnroll,Long> {

    @Query(value = "select ce from CourseEnroll ce where ce.user.userID =?1 and ce.course.courseID =?2")
    Optional<CourseEnroll> findByUserAndCourse(Long userID, Long courseID);

    Long countByCourse(Course course);

    @Query("select ce.course from CourseEnroll ce where ce.user =?1 order by ce.joinTime DESC ")
    List<Course> getCourseEnrollByUser(User user);

}
