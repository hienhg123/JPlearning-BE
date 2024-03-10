package com.in.jplearning.controllers;

import com.in.jplearning.model.Post;
import com.in.jplearning.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
@CrossOrigin("http://localhost:4200")
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<String> createPost(
            @RequestParam Map<String, String> requestMap,
            @RequestParam("file") MultipartFile file) throws IOException {
        return postService.createPost(requestMap, file);
    }

    @GetMapping("/getByUser")
    public ResponseEntity<List<Map<String, Object>>> getByUser() {
        return postService.getByUser();
    }


    @PutMapping("/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId, @RequestBody Map<String, String> requestMap) {
        return postService.updatePost(postId, requestMap);
    }

    @DeleteMapping("deletePost/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        return postService.deletePost(postId);
    }
}