package com.in.jplearning.repo;

import com.in.jplearning.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteDAO extends JpaRepository<Note,Long> {

    @Query("select n from Note n where n.user.userID =?1")
    List<Note> getAllUserNote(Long userID);
}
