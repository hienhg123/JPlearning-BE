package com.in.jplearning.repositories;

import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashCardSetDAO extends JpaRepository<FlashCardSet, Long> {
    List<FlashCardSet> findByUserSetContaining(User user);
    @Query("SELECT COUNT(f) FROM FlashCard f WHERE f.flashCardSet = :flashCardSet")
    int countFlashCardsByFlashCardSet(@Param("flashCardSet") FlashCardSet flashCardSet);


}
