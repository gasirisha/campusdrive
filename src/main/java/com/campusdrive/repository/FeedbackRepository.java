package com.campusdrive.repository;

import com.campusdrive.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.registration.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") Long eventId);

    Optional<Feedback> findByRegistration_Id(Long registrationId);
}
