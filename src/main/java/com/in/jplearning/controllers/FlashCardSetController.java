package com.in.jplearning.controllers;


import com.in.jplearning.dtos.FlashCardSetDTO;
import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import com.in.jplearning.service.FlashCardSetService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/practice/flashcardSet")
@AllArgsConstructor
@Slf4j
public class FlashCardSetController {
    private final FlashCardSetService flashCardSetService;

//    @PostMapping("/create")
//    public ResponseEntity<FlashCardSet> createFlashCardSet(@RequestBody FlashCardSet flashCardSet) {
//        FlashCardSet createdFlashCardSet = flashCardSetService.createFlashCardSet(flashCardSet);
//        if (createdFlashCardSet != null) {
//            return ResponseEntity.ok(createdFlashCardSet);
//        } else {
//            return ResponseEntity.badRequest().build();
//        }
//    }

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

//    @PostMapping("/createWithFlashCards")
//    public ResponseEntity<FlashCardSet> createFlashCardSetWithFlashCards(@RequestBody FlashCardSetDTO request) {
//        FlashCardSet createdFlashCardSet = flashCardSetService.createFlashCardSetWithFlashCards(request);
//
//        if (createdFlashCardSet != null) {
//            return new ResponseEntity<>(createdFlashCardSet, HttpStatus.CREATED);
//        } else {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PutMapping("/update/{flashCardSetId}")
    public ResponseEntity<String> updateFlashcardSet(@PathVariable Long flashCardSetId,
                                                     @RequestBody Map<String, Object> requestMap) {
        return flashCardSetService.updateFlashcard(flashCardSetId, requestMap);
    }

    @PostMapping("/createFlashCard")
    public ResponseEntity<String> createFlashcard(@RequestBody Map<String, Object> requestMap) {
        return flashCardSetService.createFlashcard(requestMap);
    }


}
