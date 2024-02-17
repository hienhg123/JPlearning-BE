package com.in.jplearning.repositories;

import com.in.jplearning.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostDAO extends JpaRepository<Post,Long> {
    Optional<Post> findById(Long postID );
}
