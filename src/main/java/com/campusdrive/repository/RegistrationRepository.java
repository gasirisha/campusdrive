package com.campusdrive.repository;

import com.campusdrive.entity.Event;
import com.campusdrive.entity.Registration;
import com.campusdrive.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByEventAndStudent(Event event, Student student);
    long countByEvent(Event event);

    Optional<Registration> findByEvent_IdAndStudent_Id(Long eventId, Long studentId);

    @Query(value = "SELECT e.id AS event_id, e.title AS title, COUNT(r.id) AS registrations " +
            "FROM event e LEFT JOIN registration r ON r.event_id = e.id " +
            "GROUP BY e.id ORDER BY registrations DESC", nativeQuery = true)
    List<Object[]> countRegistrationsPerEventNative();
}
