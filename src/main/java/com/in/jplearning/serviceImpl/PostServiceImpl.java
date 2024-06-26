package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.NotificationType;
import com.in.jplearning.enums.PostType;
import com.in.jplearning.model.*;
import com.in.jplearning.repositories.*;
import com.in.jplearning.service.PostService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostDAO postDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDAO userDAO;
    private final PostFavoriteDAO postFavoriteDAO;

    private final PostLikeDAO postLikeDAO;

    private final PostCommentDAO postCommentDAO;

    private final String cloudFront = "https://d3vco6mbl6bsb7.cloudfront.net";

    private final String bucketName = "jplearning-userpost";


    private final S3AsyncClient s3AsyncClient;

    private final TrainerDAO trainerDAO;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final NotificationDAO notificationDAO;

    private static final int MAX_POST_CONTENT_SIZE = 16 * 1024 * 1024;

    @Override
    public ResponseEntity<String> createPost(Map<String, String> requestMap){
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            User user = userOptional.get();
            //check if user is trainer
            Trainer trainer = trainerDAO.getByUserId(user.getUserID());
            if(trainer == null){
                return JPLearningUtils.getResponseEntity("Chỉ có trainer mới được đăng bài", HttpStatus.BAD_REQUEST);
            }
            if(requestMap.get("postContent")!= null && requestMap.get("postContent").getBytes().length > MAX_POST_CONTENT_SIZE){
                return JPLearningUtils.getResponseEntity("Bài đăng quá dài, vui lòng giảm bớt", HttpStatus.BAD_REQUEST);
            }

            Post post = getPostFromMap(requestMap,user);
            postDAO.save(post);
            //check if draft
            if(requestMap.get("draft").equalsIgnoreCase("true")){
                return JPLearningUtils.getResponseEntity("Lưu bản nháp thành công", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("Đăng bài thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Override
    public ResponseEntity<?> uploadFiles(MultipartFile file) {
        try{
            if (file == null || file.isEmpty()) {
                return JPLearningUtils.getResponseEntity("Không tìm thấy file", HttpStatus.BAD_REQUEST);
            }
            uploadToS3Async(file);
            Map<String, String> response = new HashMap<>();
            String imgUrl = cloudFront + "/" + file.getOriginalFilename();
            response.put("url",imgUrl);
            return  new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @Override
    public ResponseEntity<String> updatePost(Map<String, String> requestMap) {
        try {
            Optional<Post> postOptional = postDAO.findById(Long.parseLong(requestMap.get("postID")));

            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Trainer trainer = trainerDAO.getByUserId(userOptional.get().getUserID());
            if(postOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Bài đăng không tồn tại",HttpStatus.NOT_FOUND);
            }
            //check if user is trainer
            if(trainer == null){
                return JPLearningUtils.getResponseEntity("Chỉ có trainer mới được đăng bài", HttpStatus.BAD_REQUEST);
            }
            if(requestMap.get("postContent")!= null && requestMap.get("postContent").getBytes().length > MAX_POST_CONTENT_SIZE){
                return JPLearningUtils.getResponseEntity("Bài đăng quá dài, vui lòng giảm bớt", HttpStatus.BAD_REQUEST);
            }
            Post post = postOptional.get();
            post.setPostContent(requestMap.get("postContent"));
            post.setTitle(requestMap.get("title"));
            post.setIsDraft(Boolean.parseBoolean(requestMap.get("draft")));
            post.setPostType(PostType.valueOf(requestMap.get("postType")));
            post.setLevel(JLPTLevel.valueOf(requestMap.get("level")));
            postDAO.save(post);
            //check if draft
            if(requestMap.get("draft").isEmpty()){
                return JPLearningUtils.getResponseEntity("Lưu thay đổi thành công", HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("Đăng thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<?> getByUserPostNotDraft(int pageNumber,int pageSize) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            return new ResponseEntity<>(postDAO.getByUserPostNotDraft(jwtAuthFilter.getCurrentUser(), pageable), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getUserFavorite(int pageNumber, int pageSize) {
        try {
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            return new ResponseEntity<>(postFavoriteDAO.findPostsByUserFavorite(userOptional.get(),pageable), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    @Override
    public ResponseEntity<?> deletePost(Long postId) {
        try{
            Optional<Post> postOptional = postDAO.findById(postId);
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            if(postOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity("Bài đăng không tồn tại", HttpStatus.NOT_FOUND);
            }
            if(!postOptional.get().getUser().equals(userOptional.get())){
                return JPLearningUtils.getResponseEntity("Bạn không có quyền xóa bài viết này", HttpStatus.NOT_FOUND);
            }
            List<PostComment> postCommentList = postCommentDAO.getByPostID(postId);
            List<PostLike> postLikeList = postLikeDAO.getByPostID(postId);
            List<PostFavorite> postFavorites = postFavoriteDAO.getByPostID(postId);
            List<Notification> notificationList = notificationDAO.getNotificationByPostID(postId);
            //delete all the constrain
            postFavoriteDAO.deleteAll(postFavorites);
            postLikeDAO.deleteAll(postLikeList);
            postCommentDAO.deleteAll(postCommentList);
            notificationDAO.deleteAll(notificationList);
            postDAO.deleteById(postId);
            return JPLearningUtils.getResponseEntity("Xóa bài viết thành công", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getFeaturedPost() {
        try{
            List<Post> postList = postDAO.findAll();
            List<Post> featuredPost = postList.stream()
                    .sorted(Comparator.comparing(post -> post.getPostLikes().size(), Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(featuredPost, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getByUserPostDraft(int pageNumber, int pageSize) {
        try{
            if(jwtAuthFilter.getCurrentUser().isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.REQUIRED_LOGIN, HttpStatus.BAD_REQUEST);
            }
            Optional<User> userOptional = userDAO.findByEmail(jwtAuthFilter.getCurrentUser());
            if(userOptional.isEmpty()){
                return JPLearningUtils.getResponseEntity(JPConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            return new ResponseEntity<>(postDAO.getByUserPostDraft(jwtAuthFilter.getCurrentUser(), pageable), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> getByUserFavorites() {
        try {
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).orElse(null);

            if (user != null) {
                List<PostFavorite> userFavorites = postFavoriteDAO.findByUser(user);

                List<Map<String, Object>> favoritesList = new ArrayList<>();

                for (PostFavorite postFavorite : userFavorites) {
                    Map<String, Object> postMap = mapPostToDto(postFavorite.getPost());
                    favoritesList.add(postMap);
                }

                return new ResponseEntity<>(favoritesList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public ResponseEntity<?> getAllPost(int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            return new ResponseEntity<>(postDAO.getAllPost(pageable), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> getPostById(Long postID) {
        try{
            //check post exist
            Optional<Post> postOptional = postDAO.findById(postID);
            if(postOptional.isPresent()){
                Post post = postOptional.get();
                List<PostComment> topLevelComments = post.getPostComments().stream()
                        .filter(comment -> comment.getParentComment() == null)
                        .collect(Collectors.toList());
                topLevelComments.forEach(this::fetchChildComments);
                post.setPostComments(topLevelComments);
                return new ResponseEntity<>(post, HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("Bài viết không tồn tại", HttpStatus.BAD_REQUEST);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Post(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private CompletableFuture<PutObjectResponse> uploadToS3Async(MultipartFile file) throws IOException {
        return s3AsyncClient.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename())
                        .build(),
                AsyncRequestBody.fromInputStream(file.getInputStream(), file.getSize(), executorService));
    }

    private Post getPostFromMap(Map<String, String> requestMap, User user) {
        return Post.builder()
               .postContent(requestMap.get("postContent"))
               .title(requestMap.get("title"))
               .postType(PostType.valueOf(requestMap.get("postType")))
               .level(JLPTLevel.valueOf(requestMap.get("level")))
               .isDraft(Boolean.parseBoolean(requestMap.get("draft")))
               .createdAt(LocalDateTime.now())
               .user(user)
               .build();

    }
    private Map<String, Object> mapPostToDto(Post post) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postID", post.getPostID());
        postMap.put("title", post.getTitle());
        postMap.put("postContent", post.getPostContent());
        postMap.put("createdAt", post.getCreatedAt());
        return postMap;
    }
    private void fetchChildComments(PostComment parentComment) {
        List<PostComment> childComments = parentComment.getChildComments();
        if (childComments.isEmpty()) {
            return; // Base case: no more child comments, so return
        }

        // Process child comments recursively
        for (PostComment childComment : childComments) {
            // Process the current child comment

            // Recursively fetch child comments of the current child comment
            fetchChildComments(childComment);
        }
    }

}
