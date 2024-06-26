package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Report;
import com.in.jplearning.service.ReportService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    @GetMapping(path = "/getAllReport/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getReportList(@PathVariable int pageNumber ,@PathVariable int pageSize ){
        try{
            return reportService.getReportList(pageNumber,pageSize);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @DeleteMapping("deleteReport/{postID}")
    public ResponseEntity<String> deleteReport(@PathVariable Long postID){
        try{
            return reportService.deleteReport(postID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @DeleteMapping("deleteTrainerPost/{postID}")
    public ResponseEntity<?> deleteTrainerPost(@PathVariable Long postID){
        try{
            return reportService.deleteTrainerPost(postID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
