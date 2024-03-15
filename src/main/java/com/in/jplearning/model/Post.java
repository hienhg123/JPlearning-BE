package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_ID")
    private Long postID;
    private String title;
    private String postContent;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    private String fileUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk",referencedColumnName = "user_ID")
    private User user;


    @OneToMany(mappedBy = "post")
    List<PostComment> postComments;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    List<PostLike> postLikes;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    List<PostFavorite> postFavorites;

    @Transient
    private Long numberOfComments;

    @Transient
    private Long numberOfLikes;

    @Transient
    public Long getNumberOfComments() {
        return postComments != null ? (long) postComments.size() : 0;
    }

    @Transient
    public Long getNumberOfLikes() {
        return postLikes != null ? (long) postLikes.size() : 0;
    }

    public void addComment(PostComment comment) {
        if (postComments == null) {
            postComments = new ArrayList<>();
        }
        postComments.add(comment);
        comment.setPost(this);
    }

    public void addLike(PostLike like) {
        if (postLikes == null) {
            postLikes = new ArrayList<>();
        }
        postLikes.add(like);
        like.setPost(this);
    }
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
}
