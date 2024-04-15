package com.in.jplearning.serviceImpl;


import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.repositories.FlashCardDAO;
import com.in.jplearning.repositories.FlashCardSetDAO;
import com.in.jplearning.service.FlashCardService;
import com.in.jplearning.utils.JPLearningUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class FlashCardServiceImpl implements FlashCardService {
    private final FlashCardDAO flashCardDAO;
    private final FlashCardSetDAO flashCardSetDAO;

    @Override
    public List<FlashCard> createMultipleFlashCardsInSet(List<FlashCard> flashCards, Long flashCardSetId) {
        Optional<FlashCardSet> flashCardSetOptional = flashCardSetDAO.findById(flashCardSetId);

        if (flashCardSetOptional.isPresent()) {
            FlashCardSet flashCardSet = flashCardSetOptional.get();

            for (FlashCard flashCard : flashCards) {
                flashCard.setFlashCardSet(flashCardSet);
            }

            return flashCardDAO.saveAll(flashCards);
        } else {
            // FlashCardSet not found, handle accordingly (return an empty list, log a warning, etc.)
            return List.of(); // You can also return null if preferred
        }
    }
    @Override
    public List<FlashCard> getFlashCardsByFlashCardSetId(Long flashCardSetId) {
        Optional<FlashCardSet> flashCardSetOptional = flashCardSetDAO.findById(flashCardSetId);
        if (flashCardSetOptional.isPresent()) {
            FlashCardSet flashCardSet = flashCardSetOptional.get();

            // Retrieve all flashcards associated with the current flashCardSet
            List<FlashCard> flashCards = flashCardDAO.findByFlashCardSet(flashCardSetId);

            // Set the flashCardCount in the flashCardSet
            flashCardSet.setFlashCardCount(flashCards.size());

            log.info("Retrieved flashcards using query: {}", flashCards);
            return flashCards;
        } else {
            log.warn("FlashCardSet with ID {} not found.", flashCardSetId);
            return Collections.emptyList();
        }
    }
    public void updateFlashCardInSet(Long flashCardSetId, Long flashCardId, FlashCard updatedFlashCard) {
        // Load the FlashCardSet along with the associated FlashCard
        FlashCardSet flashCardSet = flashCardSetDAO.findById(flashCardSetId)
                .orElseThrow(() -> new EntityNotFoundException("FlashCardSet not found with id: " + flashCardSetId));

        FlashCard flashCardToUpdate = flashCardDAO.findById(flashCardId)
                .orElseThrow(() -> new EntityNotFoundException("FlashCard not found with id: " + flashCardId));

        // Make changes to the loaded FlashCard
        flashCardToUpdate.setQuestion(updatedFlashCard.getQuestion());
        flashCardToUpdate.setAnswer(updatedFlashCard.getAnswer());
        //  Save the changes back to the database
        flashCardDAO.save(flashCardToUpdate);
        flashCardSetDAO.save(flashCardSet);
    }

    @Override
    public ResponseEntity<String> deleteFlashCard(Long flashCardId) {
        try {
            // Check if the flashcard exists
            FlashCard flashCard = flashCardDAO.findById(flashCardId)
                    .orElseThrow(() -> new EntityNotFoundException("FlashCard not found with id: " + flashCardId));

            // Remove the flashcard from its flashcard set
            FlashCardSet flashCardSet = flashCard.getFlashCardSet();
            if (flashCardSet != null) {
                flashCardSet.getFlashCards().remove(flashCard);
                flashCardSetDAO.save(flashCardSet);
            }

            // Delete the flashcard
            flashCardDAO.delete(flashCard);

            return JPLearningUtils.getResponseEntity("Xóa FlashCard thành công", HttpStatus.OK);
        } catch (DataAccessException ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





}



