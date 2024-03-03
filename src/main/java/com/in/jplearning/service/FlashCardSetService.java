package com.in.jplearning.service;

import com.in.jplearning.dtos.FlashCardSetDTO;
import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface FlashCardSetService {
//    FlashCardSet createFlashCardSet(FlashCardSet flashCardSet);
    List<FlashCardSet> getAllFlashCardSetsForCurrentUserWithFlashCardCount();
//    FlashCardSet createFlashCardSetWithFlashCards(FlashCardSetDTO request);
    ResponseEntity<String> updateFlashcard(Long flashCardSetId, Map<String, Object> requestMap);
    ResponseEntity<String> createFlashcard(Map<String, Object> requestMap);
    Map<String, Object> getFlashCardsByFlashCardSetId(Long flashCardSetId);

    List<FlashCardSet> getAllFlashCardSets();

    ResponseEntity<FlashCardSet> findByID(Long flashcardSetID);
}
