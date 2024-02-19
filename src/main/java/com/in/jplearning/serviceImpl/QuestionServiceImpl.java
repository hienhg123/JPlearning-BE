package com.in.jplearning.serviceImpl;

import com.in.jplearning.dtos.QuestionDTO;
import com.in.jplearning.repositories.QuestionDAO;
import com.in.jplearning.service.QuestionService;
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
public class QuestionServiceImpl implements QuestionService {

    private final QuestionDAO questionDAO;
    @Override
    public ResponseEntity<List<QuestionDTO>> getExerciseQuestion(Long exerciseID) {
        try{
            log.info(String.valueOf(exerciseID));
            return new ResponseEntity<>(questionDAO.getByExerciseId(exerciseID),HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
