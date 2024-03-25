package com.in.jplearning.serviceImpl;

import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Course;
import com.in.jplearning.model.Post;
import com.in.jplearning.repositories.CourseDAO;
import com.in.jplearning.repositories.PostDAO;
import com.in.jplearning.service.SearchService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final CourseDAO courseDAO;

    private final PostDAO postDAO;
    @Override
    public ResponseEntity<?> searchForCourseAndPosts(String value) {
        try{
            Map<String,Object> searchResult = new HashMap<>();
            List<Course> courses = courseDAO.searchByValue(value);
            List<Post> posts = postDAO.searchByValue(value);
            searchResult.put("course",courses);
            searchResult.put("posts",posts);
            return new ResponseEntity<>(searchResult, HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
