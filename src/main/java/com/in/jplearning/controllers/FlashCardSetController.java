package com.in.jplearning.controllers;

import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.service.FlashCardSetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/practice/flashcardSet")
@AllArgsConstructor
@Slf4j
public class FlashCardSetController {
    private final FlashCardSetService flashCardSetService;

    @PostMapping("/create")
    public ResponseEntity<FlashCardSet> createFlashCardSet(@RequestBody FlashCardSet flashCardSet) {
        FlashCardSet createdFlashCardSet = flashCardSetService.createFlashCardSet(flashCardSet);
        if (createdFlashCardSet != null) {
            return ResponseEntity.ok(createdFlashCardSet);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FlashCardSet>> getAllFlashCardSetsForCurrentUser() {
        List<FlashCardSet> flashCardSets = flashCardSetService.getAllFlashCardSetsForCurrentUserWithFlashCardCount();
        return ResponseEntity.ok(flashCardSets);
    }

    @GetMapping("/listAll")
    public ResponseEntity<List<FlashCardSet>> getAllFlashCardSets() {
        List<FlashCardSet> flashCardSets = flashCardSetService.getAllFlashCardSets();
        return ResponseEntity.ok(flashCardSets);
    }

}