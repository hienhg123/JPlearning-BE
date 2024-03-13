package com.in.jplearning.repositories;

import com.in.jplearning.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationDAO extends JpaRepository<Notification,Long> {

    @Query(value = "select n from Notification n where n.receiver.userID =?1")
    List<Notification> getByUser(Long userID);
}
