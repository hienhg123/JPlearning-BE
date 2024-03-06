package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.dtos.PostDetailsDTO;
import com.in.jplearning.model.Post;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.PostService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostDAO postDAO;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDAO userDAO;


    @Override
    public ResponseEntity<List<PostDetailsDTO>> getAllPostDetails() {
        try {
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
            log.info("id: " + user.getUserID());

            List<Object[]> postDetailsList = postDAO.findUserPostsDetails(user);

            List<PostDetailsDTO> postDetailsDTOList = new ArrayList<>();

            for (Object[] postDetails : postDetailsList) {
                Post post = (Post) postDetails[0];
                String commentContent = (String) postDetails[1];
                Long numberOfLikes = (Long) postDetails[2];

                PostDetailsDTO postDetailsDTO = new PostDetailsDTO(
                        post.getTitle(),
                        post.getPostContent(),
                        post.getCreatedAt(),
                        post.getFileUrl(),
                        commentContent,
                        numberOfLikes
                );

                postDetailsDTOList.add(postDetailsDTO);
            }

            return new ResponseEntity<>(postDetailsDTOList, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
