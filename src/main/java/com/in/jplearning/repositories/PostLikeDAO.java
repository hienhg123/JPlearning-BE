package com.in.jplearning.repositories;

import com.in.jplearning.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostLikeDAO extends JpaRepository<PostLike,Long> {
    @Query("select l from PostLike l where l.user.email =?1 and l.post.postID = ?2")
    Optional<PostLike> findByEmailAndPostId(String email, Long postID);
}
