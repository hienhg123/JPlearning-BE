package com.in.jplearning.service;

import com.in.jplearning.model.Notification;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    ResponseEntity<List<Notification>> getUserNotification();

    ResponseEntity<String> createPostNotification(Map<String, String> requestMap);
}
