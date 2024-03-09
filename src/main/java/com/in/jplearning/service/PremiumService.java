package com.in.jplearning.service;

import com.in.jplearning.model.Premium;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PremiumService {
    ResponseEntity<List<Premium>> getAllPremium();
}
