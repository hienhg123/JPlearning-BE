package com.in.jplearning.repositories;

import com.in.jplearning.model.Exercises;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseDAO extends JpaRepository<Exercises,Long> {
}
