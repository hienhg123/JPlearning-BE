package com.in.jplearning.repositories;

import com.in.jplearning.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentDAO extends JpaRepository<PostComment,Long> {
}
