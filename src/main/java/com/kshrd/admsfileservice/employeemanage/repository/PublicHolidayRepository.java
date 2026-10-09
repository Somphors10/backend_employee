package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.PublicHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, UUID> {
    Optional<PublicHoliday> findByHolidayDate(LocalDate holidayDate);
}
