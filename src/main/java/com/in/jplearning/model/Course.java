package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in.jplearning.enums.JLPTLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_ID")
    private Long courseID;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String courseName;
    private String courseDescription;
    @Enumerated(EnumType.STRING)
    private JLPTLevel courseLevel;
    private Boolean isFree;
    @JsonIgnoreProperties("course")
    @OneToMany(mappedBy = "course")
    private List<Chapter> chapterList;

    @JsonIgnore
    @ManyToMany()
    @JoinTable(name = "course_enroll",
            joinColumns = @JoinColumn(name = "course_fk", referencedColumnName = "course_ID"),
            inverseJoinColumns = @JoinColumn(name = "user_fk", referencedColumnName = "user_ID")
    )
    private Set<User> users = new HashSet<>();



    public void enroll(User user){
        users.add(user);
    }
}
