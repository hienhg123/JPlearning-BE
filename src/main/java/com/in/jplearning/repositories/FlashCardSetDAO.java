package com.in.jplearning.repositories;

import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FlashCardSetDAO extends JpaRepository<FlashCardSet, Long> {
    List<FlashCardSet> findByUserSetContaining(User user);
    @Query("SELECT COUNT(f) FROM FlashCard f WHERE f.flashCardSet = :flashCardSet")
    int countFlashCardsByFlashCardSet(@Param("flashCardSet") FlashCardSet flashCardSet);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_flashcard_set WHERE flash_card_set_fk = ?1", nativeQuery = true)
    void deleteAssociationsWithUsers(Long flashCardSetId);




}

