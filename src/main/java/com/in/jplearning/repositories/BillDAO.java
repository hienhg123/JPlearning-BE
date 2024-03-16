package com.in.jplearning.repositories;

import com.in.jplearning.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BillDAO extends JpaRepository<Bill,Long> {

    @Query("select b from Bill b where b.user.email =?1")
    List<Bill> getbyUser(String email);
   @Query(value= "select b from Bill b where b.user.email=?1")
    Page<Bill> findByUserEmail(String userEmail, Pageable pageable);

}
