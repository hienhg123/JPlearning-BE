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
public class Lesson implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_ID")
    private Integer lessonID;
    private String lessonTitle;
    private String lessonDescription;
    private String vocabularyMaterial;
    private String listeningMaterial;
    private String grammarMaterial;
    private String exerciseMaterial;
    private String videoMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_fk",nullable = false,referencedColumnName = "chapter_ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Chapter chapter;
}
