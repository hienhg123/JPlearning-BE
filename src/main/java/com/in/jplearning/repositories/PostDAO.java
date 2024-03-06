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
    @Query("SELECT p, pc.commentContent, COUNT(pl) " +
            "FROM Post p " +
            "LEFT JOIN PostComment pc ON p.postID = pc.post.postID " +
            "LEFT JOIN PostLike pl ON p.postID = pl.post.postID " +
            "WHERE p.user = :user " +
            "GROUP BY p.postID, pc.commentID")
    List<Object[]> findUserPostsDetails(@Param("user") User user);
}
