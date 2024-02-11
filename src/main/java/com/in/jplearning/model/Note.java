package com.in.jplearning.model;

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
public class Note implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long noteID;
    private int timePoint; //in second
    @Column(columnDefinition = "NVARCHAR(500)")
    private String note;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_fk",referencedColumnName = "lesson_ID")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk",referencedColumnName = "user_ID")
    private User user;

}
