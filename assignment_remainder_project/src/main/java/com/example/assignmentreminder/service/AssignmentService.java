package com.example.assignmentreminder.service;

import com.example.assignmentreminder.model.Assignment;
import com.example.assignmentreminder.model.AppUser;
import com.example.assignmentreminder.repository.AssignmentRepository;
import com.example.assignmentreminder.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public AssignmentService(AssignmentRepository assignmentRepository, UserRepository userRepository, JavaMailSender mailSender) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    private AppUser getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("No authenticated user found");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Assignment saveAssignment(Assignment assignment) {
        AppUser user = getCurrentUser();
        assignment.setUser(user);
        return assignmentRepository.save(assignment);
    }
    public List<Assignment> getAllAssignments() {
        AppUser user = getCurrentUser();
        return assignmentRepository.findByUser(user);
    }
    @Transactional
    public void deleteAssignmentByTitle(String title) {
        AppUser user = getCurrentUser();
        assignmentRepository.deleteByTitleAndUser(title, user);
    }
    @Scheduled(cron = "0 0 9 * * ?") //daily at 9 AM
    public void sendReminders() {
        List<Assignment> assignments = assignmentRepository.findAll();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        for (Assignment a : assignments) {
            if (a.getDeadline() != null && a.getDeadline().equals(tomorrow) && a.getStudentEmail() != null) {
                sendEmailReminder(a);
            }
        }
    }

    private void sendEmailReminder(Assignment assignment) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(assignment.getStudentEmail());
            message.setSubject("📢 Assignment Reminder: " + assignment.getTitle());
            message.setText(
                    "Hello,\n\n" +
                            "Your assignment **" + assignment.getTitle() + "** is due tomorrow (**" + assignment.getDeadline() + "**).\n\n" +
                            "**Description:** " + assignment.getDescription() + "\n\n" +
                            "Please make sure to submit it on time!\n\n" +
                            "Best regards,\nAssignment Reminder Team"
            );
            mailSender.send(message);
            System.out.println("Email sent to: " + assignment.getStudentEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
