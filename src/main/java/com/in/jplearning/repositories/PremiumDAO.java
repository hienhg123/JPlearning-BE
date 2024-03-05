package com.in.jplearning.repositories;

import com.in.jplearning.model.Premium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumDAO extends JpaRepository<Premium,Long> {
}
