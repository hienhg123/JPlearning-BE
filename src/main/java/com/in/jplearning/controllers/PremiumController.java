package com.in.jplearning.controllers;

import com.in.jplearning.model.Premium;
import com.in.jplearning.service.PremiumService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/premium")
@AllArgsConstructor
@CrossOrigin("http://localhost:4200")
public class PremiumController {

    private final PremiumService premiumService;

    @GetMapping(path = "/getAllPremium")
    public ResponseEntity<List<Premium>> getAllPremium(){
        try{
            return premiumService.getAllPremium();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
