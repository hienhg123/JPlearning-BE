package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.enums.NotificationType;
import com.in.jplearning.model.Notification;
import com.in.jplearning.model.Post;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.NotificationDAO;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.NotificationService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDAO notificationDAO;

    private final UserDAO userDAO;

    private final PostDAO postDAO;

    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<List<Notification>> getUserNotification() {
        try {
            User receiver = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            List<Notification> notifications = notificationDAO.getByUser(receiver.getUserID());
            if (notifications.size() > 5) {
                notifications = notifications.subList(0, 5);
            }
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> createPostNotification(Map<String, String> requestMap) {
        try {
            //get the post
            Optional<Post> postOptional = postDAO.findById(Long.parseLong(requestMap.get("postID")));
            //check if empty
            if (!postOptional.isEmpty()) {
                Post post = postOptional.get();
                //get the content of notification
                Notification notification = getNotificationFromMap(requestMap, post.getUser().getUserID());
                //save the notification
                notificationDAO.save(notification);
                return JPLearningUtils.getResponseEntity("", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateAllReadStatus(List<Notification> notifications) {
        try{
            //set all read to true
            for(Notification notification: notifications){
                notification.setRead(true);
                notificationDAO.save(notification);
            }
            return JPLearningUtils.getResponseEntity("", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Notification getNotificationFromMap(Map<String, String> requestMap, Long userID) {
        User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
        User sender = User.builder()
                .userID(user.getUserID())
                .build();
        User receiver = User.builder()
                .userID(userID)
                .build();
        Notification notification = Notification.builder()
                .notificationType(NotificationType.valueOf(requestMap.get("notificationType")))
                .receiver(receiver)
                .content(requestMap.get("content"))
                .isRead(false)
                .sender(sender)
                .createdTime(LocalDateTime.now())
                .build();
        return notification;
    }
}
