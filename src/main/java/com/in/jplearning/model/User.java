package com.in.jplearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_ID")
    private Long userID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Date dob;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private boolean isActive;

    @OneToMany(mappedBy = "user")
    private List<UserJLPTExercise> userJLPTExerciseList;

    @ManyToMany(mappedBy = "userSet")
    private Set<FlashCardSet> cardSets = new HashSet<>();

}
