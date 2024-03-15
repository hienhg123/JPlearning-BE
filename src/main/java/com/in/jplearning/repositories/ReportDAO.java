package com.in.jplearning.repositories;

import com.in.jplearning.model.Premium;
import com.in.jplearning.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportDAO extends JpaRepository<Report,Long> {
}
