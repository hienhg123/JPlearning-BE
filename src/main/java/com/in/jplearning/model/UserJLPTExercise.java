package com.in.jplearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
public class UserJLPTExercise implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userJLPTExercise_ID")
    private Long userJLPTExerciseID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk",referencedColumnName = "user_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jlptEx_fk",referencedColumnName = "jlptEx_ID")
    private JLPTExercise jlptExercise;

    private int mark;
    private Date submittedAt;
    private int numberOfAttempts;

}
