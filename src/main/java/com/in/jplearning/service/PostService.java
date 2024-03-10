package com.in.jplearning.service;

import com.in.jplearning.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PostService {
    ResponseEntity<String> createPost(Map<String, String> requestMap, MultipartFile file) throws IOException;
    ResponseEntity<List<Map<String, Object>>> getByUser();
    ResponseEntity<String> updatePost(Long postId, Map<String, String> requestMap);
    ResponseEntity<String> deletePost(Long postId);
    ResponseEntity<List<Map<String, Object>>> getByUserFavorites();


}
