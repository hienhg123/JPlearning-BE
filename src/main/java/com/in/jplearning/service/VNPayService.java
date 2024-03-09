package com.in.jplearning.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface VNPayService {
    ResponseEntity<?> createPayment(Long premiumID);

    ResponseEntity<String> paymentCallBack(Map<String, String> requestMap, HttpServletResponse response);
}
