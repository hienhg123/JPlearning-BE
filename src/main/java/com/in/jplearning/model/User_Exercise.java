package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in.jplearning.enums.ExerciseType;
import com.in.jplearning.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User_Exercise implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_exercise_id")
    private Long user_exercise_ID;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk",referencedColumnName = "user_ID")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_fk",referencedColumnName = "exercises_ID")
    private Exercises exercises;
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;
    private int mark;
    private int maxPoint;
    private Date submittedAt;
    private int numberOfAttempts;
    public String getTitle() {
        return this.exercises != null ? this.exercises.getTitle() : null;
    }

}
