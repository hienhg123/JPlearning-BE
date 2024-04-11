package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.service.PostInteractionService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/post-interaction")
@AllArgsConstructor
@CrossOrigin("http://localhost:4200")
public class PostInteractionControllers {
    private final PostInteractionService postInteractionService;

    @PostMapping("/like-post")
    public ResponseEntity<?> likedPost(@RequestBody Map<String,String> requestMap){
        try{
            return postInteractionService.likePost(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
    @PostMapping("/comment-post")
    public ResponseEntity<?> commentPost(@RequestBody Map<String,String> requestMap){
        try{
            return postInteractionService.commentPost(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }
    @PutMapping("/update-comment")
    public ResponseEntity<?> updateComment(@RequestBody Map<String,String> requestMap){
        try{
            return postInteractionService.updateComment(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/getCommentById/{commentID}")
    public ResponseEntity<?> getCommentById(@PathVariable Long commentID){
        try{
            return postInteractionService.getCommentById(commentID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping("/favorite-post")
    public ResponseEntity<?> favoritePost(@RequestBody Map<String,String> requestMap){
        try{
            return postInteractionService.favoritePost(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping("/like-comment")
    public ResponseEntity<?> likeComment(@RequestBody Map<String,String> requestMap){
        try{
            return postInteractionService.likeComment(requestMap);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @DeleteMapping("/delete-comment/{commentID}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentID){
        try{
            return postInteractionService.deleteComment(commentID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
