package com.in.jplearning.repo;


import com.in.jplearning.enums.Role;
import com.in.jplearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
}
