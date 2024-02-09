package com.in.jplearning.repo;

import com.in.jplearning.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterDAO extends JpaRepository<Chapter, Long> {
}
