package com.in.jplearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
public class VerifyRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_ID")
    private Long requestID;
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;
    private String url;
    private boolean approved;
    private LocalDateTime requestTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_fk",referencedColumnName = "trainee_ID")
    private Trainee trainee;
}
