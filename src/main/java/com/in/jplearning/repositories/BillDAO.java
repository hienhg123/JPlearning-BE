package com.in.jplearning.repositories;

import com.in.jplearning.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BillDAO extends JpaRepository<Bill, Long> {


    @Query("select b from Bill b where b.user.email =?1 ORDER BY b.createdAt DESC")
    List<Bill> getUserLatestBill(String email, Pageable pageable);

    @Query(value = "select b from Bill b where b.user.email=?1")
    Page<Bill> findByUserEmail(String userEmail, Pageable pageable);

}
