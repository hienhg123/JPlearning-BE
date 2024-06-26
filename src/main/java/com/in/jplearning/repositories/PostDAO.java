package com.in.jplearning.repositories;

import com.in.jplearning.model.Post;
import com.in.jplearning.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT pf.post " +
            "FROM PostFavorite pf " +
            "WHERE pf.user.email = :email")
    List<Post> findUserFavorites(@Param("email") String email);

    @Query("select p from Post p where p.title LIKE %?1% and p.isDraft = false")
    List<Post> searchByValue(String value);

    @Query("select p from Post p where p.isDraft = false order by p.createdAt desc ")
    Page<Post> getAllPost(Pageable pageable);

    @Query("select p from Post p where p.user.email = ?1 and p.isDraft = true order by p.createdAt desc")
    Page<Post> getByUserPostDraft(String email,Pageable pageable);

    @Query("select p from Post p where p.user.email = ?1 and p.isDraft = false order by p.createdAt desc")
    Page<Post> getByUserPostNotDraft(String email,Pageable pageable);

}
