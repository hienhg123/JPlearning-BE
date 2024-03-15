package com.in.jplearning.controllers;

import com.in.jplearning.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/report")
@AllArgsConstructor
@CrossOrigin("http://localhost:4200")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/create/{postId}")
    public ResponseEntity<String> createReport(
            @PathVariable("postId") Long postId,
            @RequestBody Map<String, String> reportDetails) {
        return reportService.createReport(postId, reportDetails);
    }
}
