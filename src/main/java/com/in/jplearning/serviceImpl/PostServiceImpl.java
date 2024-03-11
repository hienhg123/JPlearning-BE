package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Post;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.PostService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectAclResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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

    private final String cloudFront = "https://d3vco6mbl6bsb7.cloudfront.net";

    private final String bucketName = "jplearning-userpost";

    private final S3Client s3Client;

    private final S3AsyncClient s3AsyncClient;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


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
    public ResponseEntity<List<Post>> getAllPost() {
        try {
            return new ResponseEntity<>(postDAO.findAll(), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> createPost(Map<String, String> requestMap, List<MultipartFile> files) throws IOException {
        log.info("Inside createPost {}", requestMap);
        try {
            int imageCount = 0;
            int videoCount = 0;
            String fileUrl = "";
            //loop to check limit
            for (MultipartFile file : files) {
                String contentType = file.getContentType();
                fileUrl += cloudFront + "/" + file.getOriginalFilename() + ",";
                //check if number of image reach limit
                if (contentType != null && contentType.startsWith("image/") && imageCount < 3) {
                    imageCount++;
                    //check if number of video reach limit
                } else if (contentType != null && contentType.startsWith("video/") && videoCount < 1) {
                    videoCount++;
                } else {
                    // Invalid file type or exceeded limit
                    return JPLearningUtils.getResponseEntity("Quá số lượng cho phép", HttpStatus.BAD_REQUEST);
                }
            }
            //loop to upload to s3
            uploadToS3(files);
            Post post = getPostFromMap(requestMap, fileUrl);
            postDAO.save(post);
            return JPLearningUtils.getResponseEntity("Đăng bài thành công", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void uploadToS3(List<MultipartFile> files) throws IOException {
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

    private Post getPostFromMap(Map<String, String> requestMap, String fileUrl) {
        String postContent = requestMap.get("postContent");
        String title = requestMap.get("title");

        // Retrieve the logged-in user details from the JwtAuthFilter
        String currentUserEmail = jwtAuthFilter.getCurrentUser();

        // Fetch the user from the database using the email
        User currentUser = userDAO.findByEmail(currentUserEmail).orElse(null);

        Date currentDate = getDate();
        // Ensure the user exists before creating the post
        if (currentUser != null) {
            // Create a new Post object and set its properties
            return Post.builder()
                    .postContent(postContent)
                    .title(title)
                    .user(currentUser)
                    .fileUrl(fileUrl)
                    .createdAt(currentDate)
                    .build();
        } else {
            // Handle the case where the user does not exist
            throw new RuntimeException("Logged-in user not found");
        }
    }

    private Date getDate() {
        LocalDate currentDate = LocalDate.now();
        return Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Map<String, Object> mapPostToDto(Post post) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postID", post.getPostID());
        postMap.put("title", post.getTitle());
        postMap.put("postContent", post.getPostContent());
        postMap.put("createdAt", post.getCreatedAt());
        postMap.put("fileUrl", (post.getFileUrl() != null) ? post.getFileUrl() : "");
        postMap.put("numberOfComments", post.getNumberOfComments());
        postMap.put("numberOfLikes", post.getNumberOfLikes());
        return postMap;
    }

}
