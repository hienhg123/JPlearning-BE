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
    @JsonIgnore
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
    @JsonIgnore
    @ManyToMany(mappedBy = "userSet")
    private Set<FlashCardSet> cardSets = new HashSet<>();


    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @JsonIgnore
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String getUsername() {
        return email;
    }
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

}
