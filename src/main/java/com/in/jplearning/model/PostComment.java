package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
public class PostComment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_ID")
    private Long commentID;
    private String commentContent;
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_fk",referencedColumnName = "post_ID")
    private Post post;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk",referencedColumnName = "user_ID")
    private User user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;


    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<PostComment> childComments = new ArrayList<>();

    @OneToMany(mappedBy = "postComment" , cascade = CascadeType.ALL)
    private List<PostLike> postLikes = new ArrayList<>();

}
