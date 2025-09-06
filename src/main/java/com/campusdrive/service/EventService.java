package com.campusdrive.service;

import com.campusdrive.entity.*;
import com.campusdrive.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventService {
    private final CollegeRepository collegeRepo;
    private final StudentRepository studentRepo;
    private final EventRepository eventRepo;
    private final RegistrationRepository registrationRepo;
    private final AttendanceRepository attendanceRepo;
    private final FeedbackRepository feedbackRepo;

    public EventService(CollegeRepository collegeRepo, StudentRepository studentRepo,
                        EventRepository eventRepo, RegistrationRepository registrationRepo,
                        AttendanceRepository attendanceRepo, FeedbackRepository feedbackRepo) {
        this.collegeRepo = collegeRepo;
        this.studentRepo = studentRepo;
        this.eventRepo = eventRepo;
        this.registrationRepo = registrationRepo;
        this.attendanceRepo = attendanceRepo;
        this.feedbackRepo = feedbackRepo;
    }

    // Register a student for an event
    @Transactional
    public Registration registerStudent(Long eventId, Long studentId) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        if (Boolean.TRUE.equals(event.getIsCancelled())) throw new RuntimeException("Event cancelled");

        Student student = studentRepo.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));

        if (event.getCapacity() != null && event.getCapacity() > 0) {
            long regs = registrationRepo.countByEvent(event);
            if (regs >= event.getCapacity()) throw new RuntimeException("Event full");
        }

        if (registrationRepo.existsByEventAndStudent(event, student)) {
            throw new RuntimeException("Already registered");
        }

        Registration r = new Registration();
        r.setEvent(event);
        r.setStudent(student);
        r.setRegisteredAt(LocalDateTime.now());
        return registrationRepo.save(r);
    }

    // Mark attendance
    @Transactional
    public Attendance markAttendance(Long eventId, Long studentId) {
        Registration r = registrationRepo.findByEvent_IdAndStudent_Id(eventId, studentId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        Attendance a = attendanceRepo.findByRegistration(r);
        if (a == null) {
            a = new Attendance();
            a.setRegistration(r);
        }
        a.setAttended(true);
        a.setCheckinTime(LocalDateTime.now());
        return attendanceRepo.save(a);
    }

    // Submit feedback
    @Transactional
    public Feedback submitFeedback(Long eventId, Long studentId, Integer rating, String comments) {
        if (rating == null || rating < 1 || rating > 5) throw new RuntimeException("Invalid rating");
        Registration r = registrationRepo.findByEvent_IdAndStudent_Id(eventId, studentId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        Optional<Feedback> existing = feedbackRepo.findByRegistration_Id(r.getId());
        Feedback f = existing.orElseGet(Feedback::new);
        f.setRegistration(r);
        f.setRating(rating);
        f.setComments(comments);
        return feedbackRepo.save(f);
    }

    // Report: Registrations per event
    public List<Map<String,Object>> registrationsPerEvent() {
        List<Object[]> raw = registrationRepo.countRegistrationsPerEventNative();
        List<Map<String,Object>> out = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String,Object> m = new HashMap<>();
            m.put("eventId", ((Number)row[0]).longValue());
            m.put("title", (String)row[1]);
            m.put("registrations", ((Number)row[2]).longValue());
            out.add(m);
        }
        return out;
    }

    // Report: Attendance %
    public Map<String,Object> attendancePercentage(Long eventId) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        long total = registrationRepo.countByEvent(event);
        long attended = attendanceRepo.countByRegistration_Event_IdAndAttendedTrue(eventId);
        double percent = total == 0 ? 0.0 : (attended * 100.0 / total);
        Map<String,Object> m = new HashMap<>();
        m.put("eventId", eventId);
        m.put("totalRegistrations", total);
        m.put("attended", attended);
        m.put("attendancePercentage", Math.round(percent * 100.0) / 100.0);
        return m;
    }

    // Report: Average feedback
    public Map<String,Object> averageFeedback(Long eventId) {
        Double avg = feedbackRepo.findAverageRatingByEventId(eventId);
        Map<String,Object> m = new HashMap<>();
        m.put("eventId", eventId);
        m.put("averageFeedback", avg);
        return m;
    }

    // Report: Student’s attended events
    public long studentEventsAttended(Long studentId) {
        return attendanceRepo.countByRegistration_Student_IdAndAttendedTrue(studentId);
    }
}
