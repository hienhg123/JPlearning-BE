package com.in.jplearning.repositories;

import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface FlashCardDAO extends JpaRepository<FlashCard, Long> {
    List<FlashCard> findByFlashCardSet(FlashCardSet flashCardSet );

    @Modifying
    @Transactional
    @Query("DELETE FROM FlashCard f WHERE f.flashCardSet = :flashCardSet")
    void deleteByFlashCardSet(@Param("flashCardSet") FlashCardSet flashCardSet);
}
