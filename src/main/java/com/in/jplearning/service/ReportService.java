package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ReportService {
    ResponseEntity<String> createReport(Long postId, Map<String, String> reportDetails);

}
