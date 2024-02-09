package com.in.jplearning.model;

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
public class JLPTExercise implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jlptEx_ID")
    private Long jlptExID;
    private String grammarUrl;
    private String listeningUrl;
    private String readingUrl;
    private String vocabularyUrl;
    private int mark;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_fk",referencedColumnName = "category_ID")
    private Category category;

    @OneToMany(mappedBy = "jlptExercise")
    private List<UserJLPTExercise> userJLPTExerciseList;

}
