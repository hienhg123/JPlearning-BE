package com.in.jplearning.dtos;

import com.in.jplearning.model.Answer;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Long questionID;
    private String content;
    private Long exerciseID;
    private Long answerID;
    private String answer;
    private boolean isCorrect;
    private String description;

    public QuestionDTO() {
    }

    public QuestionDTO(String content, Long exerciseID, List<Answer> answerDTOList) {
        this.content = content;
        this.exerciseID = exerciseID;
    }

    public QuestionDTO(String content, Long exerciseID, Long answerID, String answer, boolean isCorrect, String description) {
        this.content = content;
        this.exerciseID = exerciseID;
        this.answerID = answerID;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.description = description;
    }
}
