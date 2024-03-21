package com.in.jplearning.service;


import com.in.jplearning.model.Bill;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface VNPayService {
    ResponseEntity<?> createPayment(Long premiumID);

    ResponseEntity<String> paymentCallBack(Map<String, String> requestMap, HttpServletResponse response);

    ResponseEntity<Page<Map<String, Object>>> getBillHistoryByUser(int pageNumber, int pageSize);


}
