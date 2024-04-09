package com.in.jplearning.repositories;

import com.in.jplearning.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostCommentDAO extends JpaRepository<PostComment,Long> {

    @Query("select c from PostComment c where c.post.postID =?1")
    List<PostComment> getByPostID(Long postId);
}
