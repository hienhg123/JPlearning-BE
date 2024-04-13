package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.NotificationType;
import com.in.jplearning.enums.ReportType;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportDAO reportDAO;
    private final UserDAO userDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final PostDAO postDAO;

    private final PostCommentDAO postCommentDAO;

    private final PostLikeDAO postLikeDAO;

    private final PostFavoriteDAO postFavoriteDAO;

    private final NotificationDAO notificationDAO;


    @Override
    public ResponseEntity<String> createReport(Long postId, Map<String, String> reportDetails) {
        try {
            // Retrieve the user based on the current authentication
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            Optional<Post> postOptional = postDAO.findById(postId);
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.UNAUTHORIZED);
            }
            // Retrieve the post if postId is provided
            if(postOptional.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy bài viết");
            }

            // Create the report
            Report report = mapToReport(reportDetails);

            // Set the user
            report.setUser(userOptional.get());

            // If postId is provided, set the post
            report.setPost(postOptional.get());

            // Save the report
            reportDAO.save(report);

            return JPLearningUtils.getResponseEntity("Báo cáo thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Override
    public ResponseEntity<?> getReportList(int pageNumber ,int pageSize) {
        try{
            //check if user is manager
            if(jwtAuthFilter.isManager()){
                return new ResponseEntity<>(reportDAO.getAllReport(PageRequest.of(pageNumber,pageSize)), HttpStatus.OK);
//               return new ResponseEntity<>(reportDAO.findAll(PageRequest.of(pageNumber,pageSize,Sort.by("createdAt").descending())),HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteReport(Long reportID) {
        try{
            //check if manager
            if(!jwtAuthFilter.isManager()){
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            List<Report> reportList = reportDAO.getByPostID(reportID);
            reportDAO.deleteAll(reportList);
            return JPLearningUtils.getResponseEntity("Xóa thành công", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @Transactional
    @Override
    public ResponseEntity<String> deleteTrainerPost(Long postId) {
        try {
            Optional<Post> optionalPost = postDAO.findById(postId);
            if (optionalPost.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Không tim thấy bài đăng", HttpStatus.NOT_FOUND);
            }
            if(!jwtAuthFilter.isManager()){
                return JPLearningUtils.getResponseEntity(JPConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            List<Report> reportList = reportDAO.getByPostID(postId);
            List<PostComment> postCommentList = postCommentDAO.getByPostID(postId);
            List<PostLike> postLikeList = postLikeDAO.getByPostID(postId);
            List<PostFavorite> postFavorites = postFavoriteDAO.getByPostID(postId);
            List<Notification> notificationList = notificationDAO.getNotificationByPostID(postId);
            postFavoriteDAO.deleteAll(postFavorites);
            postLikeDAO.deleteAll(postLikeList);
            postCommentDAO.deleteAll(postCommentList);
            notificationDAO.deleteAll(notificationList);
            reportDAO.deleteAll(reportList);
            Notification notification = getFromMap(optionalPost.get());
            notificationDAO.save(notification);
            //delete all the constrain
            postDAO.deleteById(postId);
            return JPLearningUtils.getResponseEntity("Xóa thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private Report mapToReport(Map<String, String> reportDetails) {
        Report report = new Report();
        report.setReportType(ReportType.valueOf(reportDetails.get("reportType")));
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }
    private Notification getFromMap(Post post) {
        User sender = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
        return Notification.builder()
                .sender(sender)
                .receiver(post.getUser())
                .content("Bài viết của bạn đã bị xóa")
                .notificationType(NotificationType.POST)
                .createdTime(LocalDateTime.now())
                .isRead(false)
                .build();
    }
}

