package com.in.jplearning.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.in.jplearning.model.FlashCard;
import com.in.jplearning.model.FlashCardSet;
import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlashCardSetDTO {
    @JsonProperty("flashCardSet")
    private FlashCardSet flashCardSet;
    @JsonProperty("flashCards")
    private List<FlashCard> flashCards;
}
