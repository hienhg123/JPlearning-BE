package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.enums.Status;
import com.in.jplearning.model.VerifyRequest;
import com.in.jplearning.repositories.VerifyRequestDAO;
import com.in.jplearning.service.VerifyRequestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class VerifyRequestServiceImpl implements VerifyRequestService {

    private final VerifyRequestDAO verifyRequestDAO;

    private final JwtAuthFilter jwtAuthFilter;

    @Override
    public ResponseEntity<Page<VerifyRequest>> getAllPendingRequest(int pageNumber, int pageSize) {
        try{
            //check if manager
            if(jwtAuthFilter.isManager()){
                Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("requestTimestamp").descending());
                return new ResponseEntity<>(verifyRequestDAO.getAllPendingRequest(pageable),HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Page<VerifyRequest>> getAllRequest() {
        try{
            //check if manager
            if(jwtAuthFilter.isManager()){
                return new ResponseEntity<>(verifyRequestDAO.findAll(PageRequest.of(0,10)), HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
