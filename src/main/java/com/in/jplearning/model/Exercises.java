package com.in.jplearning.model;

import com.in.jplearning.enums.ExerciseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
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
    private int duration;
    @Enumerated(EnumType.STRING)
    private ExerciseType exerciseType;

    @OneToMany(mappedBy = "exercises")
    private List<User_Exercise> userExerciseList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_fk",referencedColumnName = "lesson_ID")
    private Lesson lesson;
}