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
import java.time.LocalDateTime;
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
    private Boolean isDraft;
    private String img;
    private LocalDateTime createdAt;
    @JsonIgnoreProperties("course")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Chapter> chapterList = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    private List<CourseEnroll> courseEnrolls;


    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_fk",referencedColumnName = "trainer_ID")
    private Trainer createBy;

    @OneToMany(mappedBy = "course")
    private Set<CourseFeedBack> courseFeedbacks;


    @Transient
    private Long enrolledUsersCount;

    @Transient
    private Double averageRating;

    // Method to set enrolled users count
    public void setEnrolledUsersCount() {
        if (courseEnrolls != null) {
            enrolledUsersCount = (long) courseEnrolls.size();
        } else {
            enrolledUsersCount = 0L;
        }
    }

    // Method to set average rating
    public void setAverageRating() {
        if (courseFeedbacks != null && !courseFeedbacks.isEmpty()) {
            double totalRating = 0;
            for (CourseFeedBack feedback : courseFeedbacks) {
                totalRating += feedback.getRating();
            }
            averageRating = totalRating / courseFeedbacks.size();
        } else {
            averageRating = 0.0;
        }
    }

}
