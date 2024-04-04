package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.NotificationType;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
import com.in.jplearning.service.PostInteractionService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class PostInteractionServiceImpl implements PostInteractionService {

    private final PostDAO postDAO;

    private final NotificationDAO notificationDAO;

    private final PostLikeDAO postLikeDAO;

    private final PostCommentDAO postCommentDAO;

    private final PostFavoriteDAO postFavoriteDAO;

    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<?> likePost(Map<String, String> requestMap) {
        try {
            Optional<Post> postOptional = postDAO.findById(Long.parseLong(requestMap.get("postID")));
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            Optional<PostLike> postLikeOptional = postLikeDAO.findByEmailAndPostId(jwtAuthFilter.getCurrentUser(),Long.parseLong(requestMap.get("postID")));
            //check if exist
            if (postOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Bài viết không tồn tại", HttpStatus.NOT_FOUND);
            }
            //check if user exist
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập để yêu thích bài viết", HttpStatus.UNAUTHORIZED);
            }
            //check if user have like the post
            if(postLikeOptional.isPresent()){
                postLikeDAO.deleteById(postLikeOptional.get().getLikeID());
                return JPLearningUtils.getResponseEntity("", HttpStatus.OK);
            }
            //get the like and save to the database
            PostLike postLike = PostLike.builder()
                    .post(postOptional.get())
                    .user(userOptional.get())
                    .build();
            //send the notification to the post's author
            String content = "Đã thích bài viết của bài";
            Notification notification = getNotification(postOptional, userOptional, content, requestMap.get("notificationType"));
            postLikeDAO.save(postLike);
            notificationDAO.save(notification);
            return JPLearningUtils.getResponseEntity("", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> commentPost(Map<String, String> requestMap) {
        try {
            Optional<Post> postOptional = postDAO.findById(Long.parseLong(requestMap.get("postID")));
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            //check if exist
            if (postOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Bài viết không tồn tại", HttpStatus.NOT_FOUND);
            }
            //check if user exist
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập để có thể bình luận", HttpStatus.UNAUTHORIZED);
            }
            String content = "";
            PostComment postComment;
            //check parent comment
            if (requestMap.get("parentId").isEmpty() || requestMap.get("parentId").equals("")) {
                content = "Đã bình luận vào bài viết của bạn";
                postComment = getFromMapWithoutParentComment(requestMap, postOptional.get(), userOptional.get());
            } else {
                Optional<PostComment> postCommentOptional = postCommentDAO.findById(Long.parseLong(requestMap.get("parentId")));
                //check if empty
                if (postCommentOptional.isEmpty()) {
                    return JPLearningUtils.getResponseEntity("Bình luận không tồn tại", HttpStatus.NOT_FOUND);
                }
                content = "Đã trời lời bạn";
                postComment = getFromMapWithParentComment(requestMap, postOptional.get(), userOptional.get(), postCommentOptional.get());
            }
            Notification notification = getNotification(postOptional, userOptional, content, requestMap.get("notificationType"));
            postCommentDAO.save(postComment);
            notificationDAO.save(notification);
            return JPLearningUtils.getResponseEntity("", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> favoritePost(Map<String, String> requestMap) {
        try {
            Optional<Post> postOptional = postDAO.findById(Long.parseLong(requestMap.get("postID")));
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            Optional<PostFavorite> postFavoriteOptional = postFavoriteDAO.findByUserEmailAndPostID(jwtAuthFilter.getCurrentUser(), Long.parseLong(requestMap.get("postID")));
            //check if exist
            if (postOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Bài viết không tồn tại", HttpStatus.NOT_FOUND);
            }
            //check if user exist
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập để lưu bài viết", HttpStatus.UNAUTHORIZED);
            }
            //check if user have like this post
            if (postFavoriteOptional.isPresent()) {
                postFavoriteDAO.deleteById(postFavoriteOptional.get().getPostFavoriteID());
                return JPLearningUtils.getResponseEntity("Thay đổi thành công", HttpStatus.OK);
            }
            //save into the database
            PostFavorite postFavorite = PostFavorite.builder()
                    .post(postOptional.get())
                    .user(userOptional.get())
                    .build();
            postFavoriteDAO.save(postFavorite);
            return JPLearningUtils.getResponseEntity("Lưu thành công", HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> likeComment(Map<String, String> requestMap) {
        try {
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            Optional<PostComment> postCommentOptional = postCommentDAO.findById(Long.parseLong(requestMap.get("commentID")));
            if (postCommentOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Bình luận không tồn tại", HttpStatus.NOT_FOUND);
            }
            //check if user exist
            if (userOptional.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
            }
            PostLike postLike = PostLike.builder()
                    .user(userOptional.get())
                    .postComment(postCommentOptional.get())
                    .build();
            postLikeDAO.save(postLike);
            return JPLearningUtils.getResponseEntity("", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> deleteComment(Long commentID) {
        try{
            Optional<PostComment> postCommentOptional = postCommentDAO.findById(commentID);
            if(postCommentOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
            }
            postCommentDAO.deleteById(commentID);
            return JPLearningUtils.getResponseEntity("",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> updateComment(Map<String, String> requestMap) {
        try{
            Optional<PostComment> postCommentOptional = postCommentDAO.findById(Long.parseLong(requestMap.get("commentID")));
            if(postCommentOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
            }
            PostComment postComment = postCommentOptional.get();
            postComment.setCommentContent(requestMap.get("commentContent"));
            postCommentDAO.save(postComment);
            return JPLearningUtils.getResponseEntity("",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private PostComment getFromMapWithParentComment(Map<String, String> requestMap, Post post, User user, PostComment postComment) {

        return PostComment.builder()
                .post(post)
                .user(user)
                .commentContent(requestMap.get("content"))
                .parentComment(postComment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PostComment getFromMapWithoutParentComment(Map<String, String> requestMap, Post post, User user) {
        return PostComment.builder()
                .post(post)
                .user(user)
                .commentContent(requestMap.get("content"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Notification getNotification(Optional<Post> postOptional, Optional<User> userOptional, String content, String notificationType) {
        User sender = userOptional.get();
        User receiver = postOptional.get().getUser();
        return Notification.builder()
                .notificationType(NotificationType.valueOf(notificationType))
                .receiver(receiver)
                .content(content)
                .sender(sender)
                .isRead(false)
                .createdTime(LocalDateTime.now())
                .build();
    }
}
