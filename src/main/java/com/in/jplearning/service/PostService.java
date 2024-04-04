package com.in.jplearning.service;

import com.in.jplearning.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PostService {
    ResponseEntity<String> createPost(Map<String, String> requestMap);
    ResponseEntity<?> getByUserPostDraft( int pageNumber, int pageSize);
    ResponseEntity<String> updatePost(Map<String, String> requestMap);
    ResponseEntity<String> deletePost(Long postId);
    ResponseEntity<List<Map<String, Object>>> getByUserFavorites();



    ResponseEntity<?> getAllPost(int pageNumber, int pageSize);

    ResponseEntity<?> getPostById(Long postID);

    ResponseEntity<?> uploadFiles(MultipartFile file);

    ResponseEntity<?> getByUserPostNotDraft(int pageNumber, int pageSize);
}
