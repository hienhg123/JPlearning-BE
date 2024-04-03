package com.in.jplearning.dtos;

import com.in.jplearning.model.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Post post;
    private Long numberOfReports;
    private LocalDateTime createdAt;

    public ReportDTO(Post post, Long numberOfReports, LocalDateTime createdAt) {
        this.post = post;
        this.numberOfReports = numberOfReports;
        this.createdAt = createdAt;
    }
}

