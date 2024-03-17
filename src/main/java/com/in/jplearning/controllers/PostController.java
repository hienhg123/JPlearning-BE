package com.in.jplearning.controllers;

import com.in.jplearning.model.Post;
import com.in.jplearning.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
@CrossOrigin("http://localhost:4200")
public class PostController {
    private final PostService postService;

    @PostMapping("/createPost")
    public ResponseEntity<String> createPost(
            @RequestParam Map<String, String> requestMap,
            @RequestPart(value = "files",required = false) List<MultipartFile> files) throws IOException {
        return postService.createPost(requestMap, files);
    }
    @GetMapping(path = "/getAllPost")
    public ResponseEntity<List<Post>> getAllPost(){
        try{
            return postService.getAllPost();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    @GetMapping("/favorites")
    public ResponseEntity<List<Map<String, Object>>> getByUserFavorites() {
        return postService.getByUserFavorites();
    }

    @GetMapping(path = "/getPostById/{postID}")
    public ResponseEntity<?> getPostById(@PathVariable Long postID){
        try{
            return postService.getPostById(postID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Post(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}