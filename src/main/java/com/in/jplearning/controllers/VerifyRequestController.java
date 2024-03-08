package com.in.jplearning.controllers;

import com.in.jplearning.model.VerifyRequest;
import com.in.jplearning.service.VerifyRequestService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/verifyRequest")
public class VerifyRequestController {

    private final VerifyRequestService verifyRequestService;

    @GetMapping(path = "/getAllPending/{pageNumber}/{pageSize}")
    ResponseEntity<Page<VerifyRequest>> getAllPendingRequest(@PathVariable int pageNumber, @PathVariable int pageSize) {
        try {
            return verifyRequestService.getAllPendingRequest(pageNumber, pageSize);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/getAll")
    ResponseEntity<Page<VerifyRequest>> getAllRequest() {
        try {
            return verifyRequestService.getAllRequest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
