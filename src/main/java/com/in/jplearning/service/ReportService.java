package com.in.jplearning.service;

import com.in.jplearning.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

public interface ReportService {
    ResponseEntity<String> createReport(Long postId, Map<String, String> reportDetails);


    ResponseEntity<?> getReportList(int pageNumber ,int pageSize);

    ResponseEntity<String> deleteReport(Long reportID);
}
