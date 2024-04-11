package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Post;
import com.in.jplearning.service.PostService;
import com.in.jplearning.utils.JPLearningUtils;
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

   @PostMapping(path = "/createPost")
   public ResponseEntity<String> createPost(@RequestBody Map<String,String> requestMap){
       try{
           return postService.createPost(requestMap);
       }catch (Exception ex){
           ex.printStackTrace();
       }
       return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
   }
   @PostMapping(path = "/uploadFile")
   public ResponseEntity<?> uploadFiles(@RequestParam("upload") MultipartFile file) throws IOException{
       try{
           return postService.uploadFiles(file);
       }catch (Exception ex){
           ex.printStackTrace();
       }
       return new ResponseEntity<>(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
   }
    @GetMapping(path = "/getAllPost/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getAllPost(@PathVariable int pageNumber, @PathVariable int pageSize){
        try{
            return postService.getAllPost(pageNumber,pageSize);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping(path = "/getUserFavorite/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getUserFavorite(@PathVariable int pageNumber, @PathVariable int pageSize){
        try{
            return postService.getUserFavorite(pageNumber,pageSize);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @GetMapping("/getByUserPostDraft/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getByUserPostDraft(@PathVariable int pageNumber, @PathVariable int pageSize) {
       try{
           return postService.getByUserPostDraft(pageNumber,pageSize);
       }catch (Exception ex) {
          ex.printStackTrace();
       }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/getByUserPostNotDraft/{pageNumber}/{pageSize}")
    public ResponseEntity<?> getByUserPostNotDraft(@PathVariable int pageNumber, @PathVariable int pageSize) {
        try{
            return postService.getByUserPostNotDraft(pageNumber,pageSize);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PutMapping("updatePost")
    public ResponseEntity<String> updatePost(@RequestBody Map<String, String> requestMap) {
        try{
            return postService.updatePost(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @DeleteMapping("deletePost/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try{
            return postService.deletePost(postId);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
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
    @GetMapping(path = "/getFeaturedPost")
    public ResponseEntity<?> getFeaturedPost(){
        try{
            return postService.getFeaturedPost();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}