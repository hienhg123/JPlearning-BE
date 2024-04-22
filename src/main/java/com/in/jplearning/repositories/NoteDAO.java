package com.in.jplearning.repositories;

import com.in.jplearning.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteDAO extends JpaRepository<Note,Long> {

    @Query("select n from Note n where n.user.userID =?1")
    List<Note> getAllUserNote(Long userID);

    @Query("select n from Note n where n.user.userID =?1 and n.lesson.lessonID =?2 ORDER BY n.noteID DESC ")
    List<Note> getUserNoteByLessonID(Long userID, Long lessonID);
    @Query("select n from Note n where n.lesson.chapter.course.courseID = ?1")
    List<Note> getNoteByCourseID(Long courseID);

    @Query("select n from Note n where n.lesson.chapter.course.courseID IN ?1")
    List<Note> getAllUserNoteWithEnrolledCourse(List<Long> courseID);
}
