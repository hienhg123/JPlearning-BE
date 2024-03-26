package com.in.jplearning.repositories;

import com.in.jplearning.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeDAO extends JpaRepository<PostLike,Long> {
}
