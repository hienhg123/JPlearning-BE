package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
public class FlashCardSet implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashCardSet_ID")
    private Long flashCardSetID;
    private String flashCardSetName;

    @ManyToMany
    @JoinTable(name = "user_flashcard_set",
            joinColumns = @JoinColumn(name = "flashCardSet_fk", referencedColumnName = "flashCardSet_ID"),
            inverseJoinColumns = @JoinColumn(name = "user_fk", referencedColumnName = "user_ID")
    )

    @JsonIgnore
    private Set<User> userSet = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "flashCardSet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FlashCard> flashCards = new HashSet<>();

    @Transient
    private int flashCardCount;





}
