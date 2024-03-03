package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.dtos.FlashCardSetDTO;
import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.FlashCardDAO;
import com.in.jplearning.repositories.FlashCardSetDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.FlashCardSetService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FlashCardSetServiceImpl implements FlashCardSetService {
    private final FlashCardSetDAO flashCardSetDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final FlashCardDAO flashCardDAO;
    private final UserDAO userDAO;


    @Override
    public List<FlashCardSet> getAllFlashCardSetsForCurrentUserWithFlashCardCount() {
        // Get the logged-in user's email
        String userEmail = jwtAuthFilter.getCurrentUser();

        if (userEmail != null) {
            // Fetch the corresponding User object from the database
            Optional<User> userOptional = userDAO.findByEmail(userEmail);

            if (userOptional.isPresent()) {
                User currentUser = userOptional.get();

                // Retrieve all flashcard sets associated with the current user
                List<FlashCardSet> flashCardSets = flashCardSetDAO.findByUserSetContaining(currentUser);

                // Iterate through each FlashCardSet to fetch and set the flashcard count
                for (FlashCardSet flashCardSet : flashCardSets) {
                    int flashCardCount = flashCardSetDAO.countFlashCardsByFlashCardSet(flashCardSet);
                    flashCardSet.setFlashCardCount(flashCardCount);
                }

                return flashCardSets;
            } else {
                log.warn("User not found with email: {}", userEmail);
                return Collections.emptyList();
            }
        } else {
            // Handle the case where the user is not logged in
            log.warn("User not logged in. Unable to retrieve FlashCardSets.");
            return Collections.emptyList();
        }
    }

    @Override
    public List<FlashCardSet> getAllFlashCardSets() {
        List<FlashCardSet> flashCardSets = flashCardSetDAO.findAll();

        // Iterate through each FlashCardSet to fetch and set the flashcard count
        for (FlashCardSet flashCardSet : flashCardSets) {
            int flashCardCount = flashCardSetDAO.countFlashCardsByFlashCardSet(flashCardSet);
            flashCardSet.setFlashCardCount(flashCardCount);
        }

        return flashCardSets;
    }

    @Override
    @Transactional
    public ResponseEntity<String> updateFlashcard(Long flashCardSetId, Map<String, Object> requestMap) {
        try {
            // Retrieve the existing FlashCardSet
            Optional<FlashCardSet> flashCardSetOptional = flashCardSetDAO.findById(flashCardSetId);
            if (flashCardSetOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("FlashCardSet not found", HttpStatus.NOT_FOUND);
            }

            FlashCardSet flashCardSet = flashCardSetOptional.get();

            // Map form data to FlashCardSet
            mapToFlashCardSet(requestMap, flashCardSet);

            // Map form data to List of FlashCards
            List<Map<String, String>> flashCardDataList = (List<Map<String, String>>) requestMap.get("flashCards");

            // Update existing FlashCards or add new ones
            for (Map<String, String> flashCardData : flashCardDataList) {
                String flashCardIdString = String.valueOf(flashCardData.get("flashCardID"));

                if (flashCardIdString != null && !flashCardIdString.isEmpty()) {
                    try {
                        Long flashCardId = Long.parseLong(flashCardIdString);

                        // Check if the FlashCard exists in the FlashCardSet
                        Optional<FlashCard> existingFlashCardOptional = flashCardDAO.findById(flashCardId);

                        if (existingFlashCardOptional.isPresent()) {
                            // If it exists, update the fields
                            FlashCard existingFlashCard = existingFlashCardOptional.get();
                            existingFlashCard.setQuestion(flashCardData.get("question"));
                            existingFlashCard.setAnswer(flashCardData.get("answer"));
                            // Save the updated FlashCard
                            flashCardDAO.save(existingFlashCard);
                        } else {
                            // If it doesn't exist, create a new FlashCard and associate it with the FlashCardSet
                            FlashCard newFlashCard = mapToFlashCard(flashCardData);
                            newFlashCard.setFlashCardSet(flashCardSet);
                            flashCardDAO.save(newFlashCard);
                        }
                    } catch (NumberFormatException e) {
                        // Handle the case where flashCardID is not a valid Long
                        return JPLearningUtils.getResponseEntity("Invalid flashCardID format: " + flashCardIdString, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    // Handle the case where flashCardID is null or empty
                    return JPLearningUtils.getResponseEntity("flashCardID is null or empty", HttpStatus.BAD_REQUEST);
                }
            }
            // Save the updated FlashCardSet
            flashCardSetDAO.save(flashCardSet);

            return JPLearningUtils.getResponseEntity("Update successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    @Override
    @Transactional
    public ResponseEntity<String> createFlashcard(Map<String, Object> requestMap) {
        try {
            // Map form data to FlashCardSet
            FlashCardSet flashCardSet = new FlashCardSet();

            // Map form data to FlashCardSet
            mapToFlashCardSet(requestMap, flashCardSet);

            // Save the FlashCardSet
            flashCardSetDAO.save(flashCardSet);

            // Map form data to List of FlashCards
            List<Map<String, String>> flashCardDataList = (List<Map<String, String>>) requestMap.get("flashCards");
            List<FlashCard> flashCards = flashCardDataList.stream()
                    .map(this::mapToFlashCard)
                    .collect(Collectors.toList());

            // Set the FlashCardSet for each FlashCard
            flashCards.forEach(flashCard -> flashCard.setFlashCardSet(flashCardSet));

            // Set the current user to the FlashCardSet
            String currentUserEmail = jwtAuthFilter.getCurrentUser();
            Optional<User> currentUserOptional = userDAO.findByEmail(currentUserEmail);
            currentUserOptional.ifPresent(user -> flashCardSet.getUserSet().add(user));

            // Save the list of FlashCards
            flashCardDAO.saveAll(flashCards);

            return JPLearningUtils.getResponseEntity("Save successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void mapToFlashCardSet(Map<String, Object> formData, FlashCardSet flashCardSet) {
        try {
            // Check if the key "flashCardSet" exists and is not null
            if (formData.containsKey("flashCardSet") && formData.get("flashCardSet") instanceof Map) {
                Map<String, Object> flashCardSetData = (Map<String, Object>) formData.get("flashCardSet");

                // Update existing FlashCardSet fields
                flashCardSet.setFlashCardSetName(flashCardSetData.getOrDefault("flashCardSetName", "").toString());
                flashCardSet.setFlashCardDescription(flashCardSetData.getOrDefault("flashCardDescription", "").toString());

                // Log the values for debugging using SLF4J
                log.debug("FlashCardSetName: {}", flashCardSet.getFlashCardSetName());
                log.debug("FlashCardDescription: {}", flashCardSet.getFlashCardDescription());
            } else {
                // Handle the case where "flashCardSet" is missing or not an object
                flashCardSet.setFlashCardSetName("");
                flashCardSet.setFlashCardDescription("");
            }
        } catch (NullPointerException | ClassCastException e) {
            log.error("Error mapping FlashCardSet: {}", e.getMessage());
        }
    }


    private FlashCard mapToFlashCard(Map<String, String> flashCardData) {
        try {
            // Map form data to FlashCard
            FlashCard flashCard = new FlashCard();
            flashCard.setQuestion(flashCardData.get("question"));
            flashCard.setAnswer(flashCardData.get("answer"));

            // Assuming you might have other fields to map for the FlashCard

            return flashCard;
        } catch (Exception e) {
            // Log the exception and other relevant information
            e.printStackTrace();
            System.out.println("Error mapping FlashCard: " + e.getMessage());
            System.out.println("FlashCardData: " + flashCardData);
            return null;
        }
    }






}

