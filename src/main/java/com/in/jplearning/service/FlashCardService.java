package com.in.jplearning.service;

import com.in.jplearning.model.FlashCard;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FlashCardService {
    List<FlashCard> createMultipleFlashCardsInSet(List<FlashCard> flashCards, Long flashCardSetId);

    List<FlashCard> getFlashCardsByFlashCardSetId(Long flashCardSetId);

}
