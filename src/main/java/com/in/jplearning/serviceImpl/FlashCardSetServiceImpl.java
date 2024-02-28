package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.dtos.FlashCardSetDTO;
import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.FlashCardDAO;
import com.in.jplearning.repositories.FlashCardSetDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.FlashCardSetService;
import com.in.jplearning.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class FlashCardSetServiceImpl implements FlashCardSetService {
    private final FlashCardSetDAO flashCardSetDAO;
    private final JwtAuthFilter jwtAuthFilter;
private final FlashCardDAO flashCardDAO;
    private final UserDAO userDAO;

    @Override
    public FlashCardSet createFlashCardSet(FlashCardSet flashCardSet) {
        // Get the logged-in user's email
        String userEmail = jwtAuthFilter.getCurrentUser();

        if (userEmail != null) {
            // Fetch the corresponding User object from the database
            Optional<User> userOptional = userDAO.findByEmail(userEmail);

            if (userOptional.isPresent()) {
                User currentUser = userOptional.get();

                // Set the logged-in user to the User set in FlashCardSet
                flashCardSet.getUserSet().add(currentUser);

                // Set the FlashCards' FlashCardSet reference to the current FlashCardSet
                if (flashCardSet.getFlashCards() != null) {
                    flashCardSet.getFlashCards().forEach(flashCard -> flashCard.setFlashCardSet(flashCardSet));
                }

                // Save the FlashCardSet
                FlashCardSet savedFlashCardSet = flashCardSetDAO.save(flashCardSet);

                if (savedFlashCardSet != null) {
                    return savedFlashCardSet;
                } else {
                    log.warn("Failed to save FlashCardSet.");
                    return null;
                }
            } else {
                log.warn("User not found with email: {}", userEmail);
                return null;
            }
        } else {
            // Handle the case where the user is not logged in
            log.warn("User not logged in. Unable to create FlashCardSet.");
            return null;
        }
    }

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
    public FlashCardSet createFlashCardSetWithFlashCards(FlashCardSetDTO request) {
        // Get the logged-in user's email
        String userEmail = jwtAuthFilter.getCurrentUser();

        if (userEmail != null) {
            // Fetch the corresponding User object from the database
            Optional<User> userOptional = userDAO.findByEmail(userEmail);

            if (userOptional.isPresent()) {
                User currentUser = userOptional.get();

                FlashCardSet flashCardSet = request.getFlashCardSet();
                List<FlashCard> flashCards = request.getFlashCards();

                if (flashCardSet != null && flashCards != null) {
                    flashCardSet.getUserSet().add(currentUser);
                    flashCards.forEach(flashCard -> flashCard.setFlashCardSet(flashCardSet));

                    FlashCardSet savedFlashCardSet = flashCardSetDAO.save(flashCardSet);

                    if (savedFlashCardSet != null) {
                        flashCardDAO.saveAll(flashCards);
                        return savedFlashCardSet;
                    } else {
                        log.warn("Failed to save FlashCardSet.");
                        return null;
                    }
                } else {
                    log.warn("FlashCardSet or FlashCards is null.");
                    return null;
                }
            } else {
                log.warn("User not found with email: {}", userEmail);
                return null;
            }
        } else {
            log.warn("User not logged in. Unable to create FlashCardSet with FlashCards.");
            return null;
        }
    }



}

