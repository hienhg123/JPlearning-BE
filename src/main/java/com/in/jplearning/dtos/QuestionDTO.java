package com.in.jplearning.dtos;

import com.in.jplearning.model.Answer;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
public class QuestionDTO {
    private Long questionID;
    private String content;
    private Long exerciseID;
    private List<Answer> answerList;

    public QuestionDTO(Long questionID,String content, Long exerciseID, List<Answer> answerList) {
        this.questionID = questionID;
        this.content = content;
        this.exerciseID = exerciseID;
        this.answerList = answerList;
    }

}
