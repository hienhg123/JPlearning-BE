package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Post;
import com.in.jplearning.model.PostFavorite;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.repositories.PostFavoriteDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.PostService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostDAO postDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDAO userDAO;
    private final PostFavoriteDAO postFavoriteDAO;



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



    private Map<String, Object> mapPostToDto(Post post) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("postID",post.getPostID());
        postMap.put("title", post.getTitle());
        postMap.put("postContent", post.getPostContent());
        postMap.put("createdAt", post.getCreatedAt());
        postMap.put("fileUrl", (post.getFileUrl() != null) ? post.getFileUrl() : "");
        postMap.put("numberOfComments", post.getNumberOfComments());
        postMap.put("numberOfLikes", post.getNumberOfLikes());
        return postMap;
    }


    @Override
    public ResponseEntity<String> createPost(Map<String, String> requestMap, MultipartFile file) throws IOException {
        log.info("Inside createPost {}", requestMap);
        try {
            Post post = getPostFromMap(requestMap);

            if (file != null && !file.isEmpty()) {
                String fileUrl = uploadFileToS3(file);
                post.setFileUrl(fileUrl);
            }

            postDAO.save(post);
            return JPLearningUtils.getResponseEntity("Post successfully created", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    private Post getPostFromMap(Map<String, String> requestMap) {
        String postContent = requestMap.get("postContent");
        String title = requestMap.get("title");

        // Retrieve the logged-in user details from the JwtAuthFilter
        String currentUserEmail = jwtAuthFilter.getCurrentUser();

        // Fetch the user from the database using the email
        User currentUser = userDAO.findByEmail(currentUserEmail).orElse(null);

        // Ensure the user exists before creating the post
        if (currentUser != null) {
            // Create a new Post object and set its properties
            return Post.builder()
                    .postContent(postContent)
                    .title(title)
                    .user(currentUser)
                    .build();
        } else {
            // Handle the case where the user does not exist
            throw new RuntimeException("Logged-in user not found");
        }
    }

    private String uploadFileToS3(MultipartFile file) throws IOException {
        // Check if the file is present
        if (file != null && !file.isEmpty()) {
            // AWS S3 configuration
            String accessKey = "your-access-key";
            String secretKey = "your-secret-key";
            String region = "your-region";
            String bucketName = "your-s3-bucket-name";

            // Create an S3 client
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();

            // Generate a unique file name or use the original file name
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Upload the file to S3 bucket
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build(), RequestBody.fromBytes(file.getBytes()));

            // Return the S3 file URL
            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
        }

        // Return an empty string or null if no file was provided
        return null;
    }



}
