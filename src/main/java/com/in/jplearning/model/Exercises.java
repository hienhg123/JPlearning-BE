package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in.jplearning.enums.ExerciseType;
import com.in.jplearning.enums.JLPTLevel;
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
public class Exercises implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercises_ID")
    private Long exercisesID;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private ExerciseType exerciseType;
    @Enumerated(EnumType.STRING)
    private JLPTLevel jlptLevel;
    private String listeningUrl;
    @Column(columnDefinition = "LONGTEXT")
    private String readingUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "exercises")
    private List<User_Exercise> userExerciseList;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_fk",referencedColumnName = "lesson_ID")
    private Lesson lesson;

    @JsonIgnoreProperties("exercises")
    @OneToMany(mappedBy = "exercises")
    private List<Question> questions = new ArrayList<>();

}
