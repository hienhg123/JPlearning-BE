package com.in.jplearning.controllers;


import com.in.jplearning.model.FlashCard;
import com.in.jplearning.service.FlashCardService;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/practice/flashcard")
@AllArgsConstructor
public class FlashCardController {
    private final FlashCardService flashCardService;

    @PostMapping("/create/{flashCardSetId}")
    public ResponseEntity<List<FlashCard>> createMultipleFlashCardsInSet(
            @RequestBody List<FlashCard> flashCards,
            @PathVariable Long flashCardSetId) {
        try {
            List<FlashCard> createdFlashCards = flashCardService.createMultipleFlashCardsInSet(flashCards, flashCardSetId);
            return ResponseEntity.ok(createdFlashCards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/bySet/{flashCardSetId}")
    public ResponseEntity<List<FlashCard>> getFlashCardsByFlashCardSetId(@PathVariable Long flashCardSetId) {
        List<FlashCard> flashCards = flashCardService.getFlashCardsByFlashCardSetId(flashCardSetId);
        return ResponseEntity.ok(flashCards);
    }

    @PutMapping("/update/{flashCardSetId}/cards/{flashCardId}")
    public ResponseEntity<String> updateFlashCardInSet(
            @PathVariable Long flashCardSetId,
            @PathVariable Long flashCardId,
            @RequestBody FlashCard updatedFlashCard
    ) {
        flashCardService.updateFlashCardInSet(flashCardSetId, flashCardId, updatedFlashCard);
        return ResponseEntity.ok("FlashCard updated successfully");
    }

}
