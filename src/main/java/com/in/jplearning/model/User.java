package com.in.jplearning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_ID")
    private Long userID;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String firstName;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String lastName;
    private String phoneNumber;
    private Date dob;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Enumerated(EnumType.STRING)
    private JLPTLevel level;
    private String gender;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String userPicture;
    private boolean isActive;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<User_Exercise> user_exercises;

    @ManyToMany(mappedBy = "userSet")
    private Set<FlashCardSet> cardSets = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "premium_fk",referencedColumnName = "premium_ID")
    private Premium premium;

    @JsonIgnore
    @ManyToMany(mappedBy = "users")
    private Set<Course> courses = new HashSet<>();
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
