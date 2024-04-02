package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.in.jplearning.enums.JLPTLevel;
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
public class Trainer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trainer_ID")
    private Long trainerID;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String fullName;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String currentJob;
    @Enumerated(EnumType.STRING)
    private JLPTLevel jlptLevel;
    private Date dob;
    private boolean isVerify;
    private int point;
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", referencedColumnName = "user_id")
    private User user;
}
