package com.in.jplearning.service;

import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;

import java.util.List;

public interface FlashCardSetService {
    FlashCardSet createFlashCardSet(FlashCardSet flashCardSet);
    List<FlashCardSet> getAllFlashCardSetsForCurrentUserWithFlashCardCount();

}
