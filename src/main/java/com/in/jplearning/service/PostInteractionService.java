package com.in.jplearning.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PostInteractionService {
    ResponseEntity<?> likePost(Map<String, String> requestMap);

    ResponseEntity<?> commentPost(Map<String, String> requestMap);

    ResponseEntity<?> favoritePost(Map<String, String> requestMap);

    ResponseEntity<?> likeComment(Map<String, String> requestMap);

    ResponseEntity<?> deleteComment(Long commentID);

    ResponseEntity<?> updateComment(Map<String, String> requestMap);

    ResponseEntity<?> getCommentById(Long commentID);
}
