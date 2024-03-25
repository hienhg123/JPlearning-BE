package com.in.jplearning.dtos;

import com.in.jplearning.model.Post;
import lombok.Data;

import java.util.Date;
@Data
public class ReportDTO {
    private Post post;
    private Long numberOfReports;
    private Date createdAt;

    public ReportDTO(Post post, Long numberOfReports, Date createdAt) {
        this.post = post;
        this.numberOfReports = numberOfReports;
        this.createdAt = createdAt;
    }
}

