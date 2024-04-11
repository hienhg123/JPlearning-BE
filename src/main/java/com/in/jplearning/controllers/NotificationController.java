package com.in.jplearning.controllers;

import com.in.jplearning.model.Notification;
import com.in.jplearning.service.NotificationService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/notification")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(path = "/getNotification")
    public ResponseEntity<List<Notification>> getAllNotification() {
        try {
            return notificationService.getUserNotification();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/createPostNotification")
    public ResponseEntity<String> createPostNotification(@RequestBody Map<String, String> requestMap) {
        try {
            return notificationService.createPostNotification(requestMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping(path = "/updateReadAllStatus")
    public ResponseEntity<String> updateAllReadStatus(@RequestBody List<Notification> notifications) {
        try {
            return notificationService.updateAllReadStatus(notifications);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestBody Notification notification) {
        try {
            return notificationService.updateStatus(notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
