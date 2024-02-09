package com.in.jplearning.repo;

import com.in.jplearning.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteDAO extends JpaRepository<Note,Long> {
}
