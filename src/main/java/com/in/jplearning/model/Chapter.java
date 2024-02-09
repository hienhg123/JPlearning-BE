package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
public class Chapter implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="chapter_ID")
    private Integer chapterID;
    private String chapterTitle;
    private String chapterDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_fk", nullable = false, referencedColumnName = "course_ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Course course;
}
