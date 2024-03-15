package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.ReportType;
import com.in.jplearning.model.Post;
import com.in.jplearning.model.Report;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.repositories.ReportDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.ReportService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportDAO reportDAO;
    private final UserDAO userDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final PostDAO postDAO;


    @Override
    public ResponseEntity<String> createReport(Long postId, Map<String, String> reportDetails) {
        try {
            // Retrieve the user based on the current authentication
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            // Retrieve the post if postId is provided
            Post post = null;
            if (postId != null) {
                post = postDAO.findById(postId).orElse(null);
                if (post == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
                }
            }

            // Create the report
            Report report = mapToReport(reportDetails);

            // Set the user
            report.setUser(user);

            // If postId is provided, set the post
            if (post != null) {
                report.setPost(post);
            }

            // Save the report
            reportDAO.save(report);

            return ResponseEntity.ok("Report created successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }
    @Override
    public ResponseEntity<Page<Report>> getReportList() {
        try{
            //check if user is manager
            if(jwtAuthFilter.isManager()){
               return new ResponseEntity<>(reportDAO.findAll(PageRequest.of(0,10,Sort.by("createdAt").descending())),HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteReport(Long reportID) {
        try{
            //check if manager
            if(jwtAuthFilter.isManager()){
                reportDAO.deleteById(reportID);
                return JPLearningUtils.getResponseEntity("Xóa thành công", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private Report mapToReport(Map<String, String> reportDetails) {
        Report report = new Report();
        report.setReportType(ReportType.valueOf(reportDetails.get("reportType")));
        report.setReportContent(reportDetails.get("reportContent"));
        report.setCreatedAt(new Date());
        return report;
    }
}

