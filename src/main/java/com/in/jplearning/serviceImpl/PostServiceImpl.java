package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.PostType;
import com.in.jplearning.model.Post;
import com.in.jplearning.model.PostFavorite;
import com.in.jplearning.model.Trainer;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.repositories.PostFavoriteDAO;
import com.in.jplearning.repositories.TrainerDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.PostService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final String cloudFront = "https://d3vco6mbl6bsb7.cloudfront.net";

    private final String bucketName = "jplearning-userpost";


    private final S3AsyncClient s3AsyncClient;

    private final TrainerDAO trainerDAO;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public ResponseEntity<String> createPost(Map<String, String> requestMap){
        log.info("Inside createPost {}", requestMap);
        try{
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            //check if user is trainer
            Trainer trainer = trainerDAO.getByUserId(user.getUserID());
            if(trainer == null){
                return JPLearningUtils.getResponseEntity("Chỉ có trainer mới được đăng bài", HttpStatus.BAD_REQUEST);
            }

            Post post = getPostFromMap(requestMap,user);
            postDAO.save(post);
            //check if draft
            if(requestMap.get("draft").isEmpty()){
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
    public ResponseEntity<String> updatePost(Long postId, Map<String, String> requestMap) {
        try {
            // Fetch the post from the database
            Optional<Post> optionalPost = postDAO.findById(postId);
            if (optionalPost.isPresent()) {
                Post post = optionalPost.get();

                // Check if the logged-in user is the owner of the post
                String currentUserEmail = jwtAuthFilter.getCurrentUser();
                if (post.getUser().getEmail().equals(currentUserEmail)) {
                    // Update post properties
                    post.setTitle(requestMap.get("title"));
                    post.setPostContent(requestMap.get("postContent"));

                    // Save the updated post
                    postDAO.save(post);

                    return JPLearningUtils.getResponseEntity("Cập nhật thành công", HttpStatus.OK);
                } else {
                    return JPLearningUtils.getResponseEntity("Unauthorized user", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return JPLearningUtils.getResponseEntity("Không tìm thấy bài đăng", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<List<Map<String, Object>>> getByUser() {
        try {
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();

            if (user != null) {
                log.info("id: " + user.getUserID());

                List<Post> posts = postDAO.findByUser(user);

                List<Map<String, Object>> postsList = new ArrayList<>();

                for (Post post : posts) {
                    Map<String, Object> postMap = mapPostToDto(post);
                    postsList.add(postMap);
                }

                return new ResponseEntity<>(postsList, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public ResponseEntity<String> deletePost(Long postId) {
        try {
            Optional<Post> optionalPost = postDAO.findById(postId);
            if (optionalPost.isPresent()) {
                postDAO.deleteById(postId);
                return JPLearningUtils.getResponseEntity("Xóa thành công", HttpStatus.OK);
            } else {
                return JPLearningUtils.getResponseEntity("Không tim thấy bài đăng", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllPost(int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
            return new ResponseEntity<>(postDAO.findAll(pageable), HttpStatus.OK);
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
                return new ResponseEntity<>(postOptional.get(), HttpStatus.OK);
            }
            return JPLearningUtils.getResponseEntity("Bài viết không tồn tại", HttpStatus.BAD_REQUEST);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Post(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void uploadToS3(List<MultipartFile> files){
        List<CompletableFuture<PutObjectResponse>> uploadFutures = files.stream()
                .map(file -> {
                    try {
                        return uploadToS3Async(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
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

}
