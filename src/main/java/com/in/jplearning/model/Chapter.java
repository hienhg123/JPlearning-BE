package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private Long chapterID;
    private String chapterTitle;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String chapterDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_fk", nullable = false, referencedColumnName = "course_ID")
    @JsonIgnore
    private Course course;

    @OneToMany(mappedBy = "chapter",cascade = CascadeType.ALL)
    @JsonIgnoreProperties("chapter")
    private List<Lesson> lessonList = new ArrayList<>();
}
