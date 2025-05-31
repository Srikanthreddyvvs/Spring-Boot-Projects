package com.example.assignmentreminder.repository;

import com.example.assignmentreminder.model.Assignment;
import com.example.assignmentreminder.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByUser(AppUser user);
    void deleteByTitleAndUser(String title, AppUser user);
}
