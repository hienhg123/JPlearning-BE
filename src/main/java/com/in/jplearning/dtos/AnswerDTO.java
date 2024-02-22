package com.in.jplearning.dtos;

import lombok.Data;

@Data
public class AnswerDTO {
    private Long answerID;
    private String answer;
    private boolean isCorrect;
    private String description;

    public AnswerDTO() {
    }

    public AnswerDTO(Long answerID, String answer, boolean isCorrect, String description) {
        this.answerID = answerID;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.description = description;
    }
}
