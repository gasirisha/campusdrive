package com.campusdrive.repository;

import com.campusdrive.entity.Attendance;
import com.campusdrive.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Attendance findByRegistration(Registration registration);
    long countByRegistration_Event_IdAndAttendedTrue(Long eventId);
    long countByRegistration_Student_IdAndAttendedTrue(Long studentId);
}
