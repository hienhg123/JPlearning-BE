package com.in.jplearning.repositories;


import com.in.jplearning.enums.JLPTLevel;
import com.in.jplearning.enums.Role;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserDAO extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId) ;


    List<User> findByRole(Role role);
    @Query(value = "select u.email from User u where u.role ='ADMIN'")
    List<String> findAllAdminEmail();


    @Query(value = "update User u set u.isActive = :isActive where u.userID  = :id")
    @Transactional
    @Modifying
    Integer updateStatus(@Param("isActive") boolean isActive, @Param("id") Long userID);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "update User u set u.firstName = :firstName, u.lastName = :lastName, u.phoneNumber = :phoneNumber, u.dob = :dob, u.email = :email, u.password = :password, u.level = :level, u.gender = :gender, u.userPicture = :userPicture where u.userID = :id")
    @Transactional
    @Modifying
    Integer updateUserProfile(@Param("firstName") String firstName, @Param("lastName") String lastName, @Param("phoneNumber") String phoneNumber,
                              @Param("dob") Date dob, @Param("email") String email, @Param("password") String password,
                              @Param("level") JLPTLevel level, @Param("gender") String gender, @Param("userPicture") String userPicture, @Param("id") Long userID);


}

