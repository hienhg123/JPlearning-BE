package com.in.jplearning.repositories;

import com.in.jplearning.model.Post;
import com.in.jplearning.model.PostFavorite;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostFavoriteDAO extends JpaRepository<PostFavorite,Long> {
    List<PostFavorite> findByUser(User user);

    @Query("select f from PostFavorite f where f.user.email =?1 and f.post.postID =?2")
    Optional<PostFavorite> findByUserEmailAndPostID(String email, Long postID);

}
