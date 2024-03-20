package com.in.jplearning.controllers;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Bill;
import com.in.jplearning.service.VNPayService;
import com.in.jplearning.utils.JPLearningUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/payment/vnpay")
public class VNPayController {

    private final VNPayService vnPayService;

    @GetMapping("/createPayment/{premiumID}")
    public ResponseEntity<?> createPayment(@PathVariable Long premiumID){
        try{
            return vnPayService.createPayment(premiumID);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @GetMapping("/paymentCallback")
    public ResponseEntity<String> paymentCallBack(@RequestParam Map<String,String> requestMap, HttpServletResponse response){
        try{
            return vnPayService.paymentCallBack(requestMap,response);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/billHistory")
    public ResponseEntity<List<Bill>> getBillHistoryByUser() {
        try {
            ResponseEntity<List<Bill>> response = vnPayService.getBillHistoryByUser();
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
    @GetMapping("/checkout")
    public ResponseEntity<?> checkOut() {
        try {
            return vnPayService.checkOut();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
    }


}
