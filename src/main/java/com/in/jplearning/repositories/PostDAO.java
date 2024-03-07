package com.in.jplearning.repositories;

import com.in.jplearning.model.Post;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostDAO extends JpaRepository<Post,Long> {
    Optional<Post> findById(Long postID );
    List<Post> findByUser(User user);

    @Query("SELECT p, pc.commentContent, COUNT(pl) " +
            "FROM Post p " +
            "LEFT JOIN p.postComments pc " +
            "LEFT JOIN PostLike pl ON pl.post = p " +
            "WHERE p.user = :user " +
            "GROUP BY p")
    List<Object[]> findUserPostsDetails(User user);

    // Custom query to retrieve comments and likes for a specific post
    @Query("SELECT pc.commentContent, COUNT(pl) " +
            "FROM PostComment pc " +
            "LEFT JOIN PostLike pl ON pl.post = pc.post " +
            "WHERE pc.post.postID = :postId " +
            "GROUP BY pc")
    List<Object[]> getCommentsAndLikesForPost(Long postId);



}
