package com.in.jplearning.controllers;

import com.in.jplearning.dtos.PostDetailsDTO;
import com.in.jplearning.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {
    private final PostService postService;
    @PostMapping("/create")
    public ResponseEntity<String> createPost(
            @RequestParam Map<String, String> requestMap,
            @RequestParam("file") MultipartFile file) throws IOException {
        return postService.createPost(requestMap, file);
    }

    @GetMapping("/details")
    public ResponseEntity<List<PostDetailsDTO>> getAllPostDetails() {
        return postService.getAllPostDetails();
    }
}
