package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in.jplearning.enums.VerificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder

public class VerifyRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_ID")
    private Long requestID;
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;
    @Column(nullable = false)
    private String url;
    private boolean approved;
    private Date requestTimestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trainer_fk",referencedColumnName = "trainer_ID")
    private Trainer trainer;
}
