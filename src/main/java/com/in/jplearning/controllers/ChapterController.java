package com.in.jplearning.controllers;

import com.in.jplearning.model.Chapter;
import com.in.jplearning.service.ChapterService;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping(path = "/chapter")
public class ChapterController {
    private final ChapterService chapterService;

    @GetMapping(path = "/getAllChapterByCourseID/{courseID}")
    public ResponseEntity<List<Chapter>> getAllChapterByCourseID(@PathVariable Long courseID){
        try{
            return chapterService.getAllChapterByCourseID(courseID);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
