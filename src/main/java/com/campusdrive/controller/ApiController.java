package com.campusdrive.controller;

import com.campusdrive.entity.*;
import com.campusdrive.repository.*;
import com.campusdrive.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final CollegeRepository collegeRepo;
    private final StudentRepository studentRepo;
    private final EventRepository eventRepo;
    private final RegistrationRepository registrationRepo;
    private final AttendanceRepository attendanceRepo;
    private final FeedbackRepository feedbackRepo;
    private final EventService eventService;

    public ApiController(CollegeRepository collegeRepo, StudentRepository studentRepo,
                         EventRepository eventRepo, RegistrationRepository registrationRepo,
                         AttendanceRepository attendanceRepo, FeedbackRepository feedbackRepo,
                         EventService eventService) {
        this.collegeRepo = collegeRepo;
        this.studentRepo = studentRepo;
        this.eventRepo = eventRepo;
        this.registrationRepo = registrationRepo;
        this.attendanceRepo = attendanceRepo;
        this.feedbackRepo = feedbackRepo;
        this.eventService = eventService;
    }

    // 🔹 Initialize sample data
    @PostMapping("/init")
    public ResponseEntity<?> initSample() {
        feedbackRepo.deleteAll();
        attendanceRepo.deleteAll();
        registrationRepo.deleteAll();
        eventRepo.deleteAll();
        studentRepo.deleteAll();
        collegeRepo.deleteAll();

        College c1 = new College("College A");
        College c2 = new College("College B");
        collegeRepo.saveAll(Arrays.asList(c1, c2));

        Student s1 = new Student(); s1.setCollege(c1); s1.setStudentRegNo("A001"); s1.setName("Alice");
        Student s2 = new Student(); s2.setCollege(c1); s2.setStudentRegNo("A002"); s2.setName("Bob");
        Student s3 = new Student(); s3.setCollege(c2); s3.setStudentRegNo("B001"); s3.setName("Charlie");
        studentRepo.saveAll(Arrays.asList(s1,s2,s3));

        Event e1 = new Event(); e1.setCollege(c1); e1.setTitle("Hackathon 2025"); e1.setEventType("Hackathon");
        e1.setDescription("24-hour coding"); e1.setCapacity(100);
        e1.setStartTime(LocalDateTime.of(2025,9,10,9,0));
        e1.setEndTime(LocalDateTime.of(2025,9,11,9,0));
        Event e2 = new Event(); e2.setCollege(c1); e2.setTitle("AI Workshop"); e2.setEventType("Workshop");
        e2.setCapacity(30);
        e2.setStartTime(LocalDateTime.of(2025,9,12,10,0));
        e2.setEndTime(LocalDateTime.of(2025,9,12,16,0));
        eventRepo.saveAll(Arrays.asList(e1,e2));

        return ResponseEntity.ok(Map.of("status","ok"));
    }

    // 🔹 Create a college
    @PostMapping("/colleges")
    public ResponseEntity<?> createCollege(@RequestBody Map<String,String> body) {
        College c = new College(body.get("name"));
        collegeRepo.save(c);
        return ResponseEntity.status(201).body(c);
    }

    // 🔹 Create a student
    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody Map<String,Object> body) {
        Long collegeId = Long.valueOf(body.get("collegeId").toString());
        College college = collegeRepo.findById(collegeId).orElseThrow(() -> new RuntimeException("College not found"));

        Student s = new Student();
        s.setCollege(college);
        s.setStudentRegNo(body.get("studentRegNo").toString());
        s.setName(body.get("name").toString());
        s.setEmail(body.getOrDefault("email","").toString());

        studentRepo.save(s);
        return ResponseEntity.status(201).body(s);
    }

    // 🔹 Create an event
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Map<String,Object> body) {
        Long collegeId = Long.valueOf(body.get("collegeId").toString());
        College college = collegeRepo.findById(collegeId).orElseThrow(() -> new RuntimeException("College not found"));

        Event e = new Event();
        e.setCollege(college);
        e.setTitle(body.get("title").toString());
        e.setDescription((String) body.getOrDefault("description", null));
        e.setEventType((String) body.getOrDefault("eventType", null));
        e.setCapacity(body.get("capacity") != null ? Integer.valueOf(body.get("capacity").toString()) : null);

        try {
            if (body.get("startTime") != null) e.setStartTime(LocalDateTime.parse(body.get("startTime").toString()));
            if (body.get("endTime") != null) e.setEndTime(LocalDateTime.parse(body.get("endTime").toString()));
        } catch (DateTimeParseException ex) {
            // ignore bad format
        }

        eventRepo.save(e);
        return ResponseEntity.status(201).body(e);
    }

    // 🔹 Register student for an event
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String,Object> body) {
        Registration r = eventService.registerStudent(
                Long.valueOf(body.get("eventId").toString()),
                Long.valueOf(body.get("studentId").toString())
        );
        return ResponseEntity.status(201).body(r);
    }

    // 🔹 Mark attendance
    @PostMapping("/attendance")
    public ResponseEntity<?> attendance(@RequestBody Map<String,Object> body) {
        Attendance a = eventService.markAttendance(
                Long.valueOf(body.get("eventId").toString()),
                Long.valueOf(body.get("studentId").toString())
        );
        return ResponseEntity.ok(a);
    }

    // 🔹 Submit feedback
    @PostMapping("/feedback")
    public ResponseEntity<?> feedback(@RequestBody Map<String,Object> body) {
        Feedback f = eventService.submitFeedback(
                Long.valueOf(body.get("eventId").toString()),
                Long.valueOf(body.get("studentId").toString()),
                Integer.valueOf(body.get("rating").toString()),
                (String) body.getOrDefault("comments", null)
        );
        return ResponseEntity.ok(f);
    }

    // 🔹 Reports
    @GetMapping("/reports/registrations")
    public ResponseEntity<?> registrationsReport() {
        return ResponseEntity.ok(eventService.registrationsPerEvent());
    }

    @GetMapping("/reports/attendance-percentage")
    public ResponseEntity<?> attendancePercentage(@RequestParam Long eventId) {
        return ResponseEntity.ok(eventService.attendancePercentage(eventId));
    }

    @GetMapping("/reports/average-feedback")
    public ResponseEntity<?> avgFeedback(@RequestParam Long eventId) {
        return ResponseEntity.ok(eventService.averageFeedback(eventId));
    }

    @GetMapping("/reports/student-participation")
    public ResponseEntity<?> studentParticipation(@RequestParam Long studentId) {
        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "eventsAttended", eventService.studentEventsAttended(studentId)
        ));
    }

    @GetMapping("/reports/popularity")
    public ResponseEntity<?> popularity(@RequestParam(defaultValue="10") int limit) {
        List<Object[]> raw = registrationRepo.countRegistrationsPerEventNative();
        List<Map<String,Object>> out = new ArrayList<>();
        int i = 0;
        for (Object[] r : raw) {
            if (i++ >= limit) break;
            out.add(Map.of(
                    "eventId", ((Number)r[0]).longValue(),
                    "title", (String)r[1],
                    "registrations", ((Number)r[2]).longValue()
            ));
        }
        return ResponseEntity.ok(out);
    }
}
