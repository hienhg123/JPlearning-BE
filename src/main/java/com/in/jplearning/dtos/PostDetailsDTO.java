package com.in.jplearning.dtos;

import lombok.*;

import java.util.Date;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailsDTO {
    private String title;
    private String postContent;
    private Date createdAt;
    private String fileUrl;
    private String commentContent;
    private Long numberOfLikes;
}
