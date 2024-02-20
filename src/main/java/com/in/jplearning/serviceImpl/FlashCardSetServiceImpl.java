package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.FlashCardSetDAO;
import com.in.jplearning.service.FlashCardSetService;
import com.in.jplearning.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class FlashCardSetServiceImpl implements FlashCardSetService {
    private final FlashCardSetDAO flashCardSetDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserService userService;

    @Override
    public FlashCardSet createFlashCardSet(FlashCardSet flashCardSet) {
        // Get the logged-in user's email
        String userEmail = jwtAuthFilter.getCurrentUser();

        if (userEmail != null) {
            // Fetch the corresponding User object from the database
            Optional<User> userOptional = userService.getUserByEmail(userEmail);

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
            Optional<User> userOptional = userService.getUserByEmail(userEmail);

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
    }

