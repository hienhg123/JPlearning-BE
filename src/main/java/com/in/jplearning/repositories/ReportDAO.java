package com.in.jplearning.repositories;

import com.in.jplearning.model.Premium;
import com.in.jplearning.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportDAO extends JpaRepository<Report,Long> {

    @Query("SELECT new com.in.jplearning.dtos.ReportDTO(r.post,COUNT(r), MAX(r.createdAt)) FROM Report r GROUP BY r.post ORDER BY MAX(r.createdAt) DESC")
    Page<Report> getAllReport(Pageable pageable);

    @Query("select r from Report r where r.post.postID=?1")
    List<Report> getByPostID(Long postId);
}
