package com.in.jplearning.serviceImpl;

import com.in.jplearning.model.Premium;
import com.in.jplearning.repositories.PremiumDAO;
import com.in.jplearning.service.PremiumService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class PremiumServiceImpl implements PremiumService {

    private final PremiumDAO premiumDAO;
    @Override
    public ResponseEntity<List<Premium>> getAllPremium() {
        try{
            return new ResponseEntity<>(premiumDAO.findAll(), HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
