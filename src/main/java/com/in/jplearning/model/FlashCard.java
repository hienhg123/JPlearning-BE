package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class FlashCard implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashCard_ID")
    private Long flashCardID;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String question;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String answer;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcardset_fk",referencedColumnName = "flashCardSet_ID")
    private FlashCardSet flashCardSet;


}
