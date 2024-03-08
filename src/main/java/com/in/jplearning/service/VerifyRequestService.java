package com.in.jplearning.service;

import com.in.jplearning.model.VerifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VerifyRequestService {
    ResponseEntity<Page<VerifyRequest>> getAllPendingRequest(int pageNumber, int pageSize);

    ResponseEntity<Page<VerifyRequest>> getAllRequest();

}
