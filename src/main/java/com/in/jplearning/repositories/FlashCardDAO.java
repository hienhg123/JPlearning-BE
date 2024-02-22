package com.in.jplearning.repositories;

import com.in.jplearning.model.FlashCard;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface FlashCardDAO extends JpaRepository<FlashCard, Long> {
    List<FlashCard> findByFlashCardSet_FlashCardSetID(Long flashCardSetId);
}
