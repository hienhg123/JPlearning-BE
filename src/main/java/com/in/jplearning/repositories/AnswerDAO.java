package com.in.jplearning.repositories;

import com.in.jplearning.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerDAO extends JpaRepository<Answer,Long> {
}
