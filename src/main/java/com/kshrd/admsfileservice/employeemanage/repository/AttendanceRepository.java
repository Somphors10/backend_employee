package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByEmployeeId(UUID employeeId);

    List<Attendance> findByWorkDate(LocalDate workDate);

    Optional<Attendance> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate workDate);

    long countByWorkDate(LocalDate workDate);
}
