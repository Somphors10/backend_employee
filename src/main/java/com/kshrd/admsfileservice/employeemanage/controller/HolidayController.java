package com.kshrd.admsfileservice.employeemanage.controller;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.exception.ResourceNotFoundException;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.ApiResponse;
import com.kshrd.admsfileservice.employeemanage.model.entity.PublicHoliday;
import com.kshrd.admsfileservice.employeemanage.repository.PublicHolidayRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/holidays")
@Tag(name = "Holidays", description = "Public holiday calendar")
public class HolidayController {
    private final PublicHolidayRepository holidayRepository;

    public HolidayController(PublicHolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('holidays:view')")
    @Operation(summary = "List public holidays")
    public ResponseEntity<ApiResponse<List<PublicHoliday>>> getHolidays() {
        List<PublicHoliday> holidays = holidayRepository.findAll().stream()
                .sorted(Comparator.comparing(PublicHoliday::getHolidayDate))
                .toList();
        return ResponseEntity.ok(ApiResponse.of("Holidays retrieved successfully", holidays, HttpStatus.OK));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('holidays:write')")
    @Operation(summary = "Create a public holiday")
    public ResponseEntity<ApiResponse<PublicHoliday>> create(@RequestBody HolidayBody body) {
        if (body.holidayDate() == null || body.name() == null || body.name().isBlank()) {
            throw new InvalidOperationException("Name and date are required");
        }
        holidayRepository.findByHolidayDate(body.holidayDate()).ifPresent(existing -> {
            throw new InvalidOperationException("A holiday already exists on this date");
        });
        PublicHoliday saved = holidayRepository.save(PublicHoliday.builder()
                .id(UUID.randomUUID())
                .name(body.name().trim())
                .holidayDate(body.holidayDate())
                .paid(body.paid() == null || body.paid())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Holiday created successfully", saved, HttpStatus.CREATED));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('holidays:write')")
    @Operation(summary = "Update a public holiday")
    public ResponseEntity<ApiResponse<PublicHoliday>> update(@PathVariable UUID id, @RequestBody HolidayBody body) {
        PublicHoliday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));
        if (body.name() != null && !body.name().isBlank()) {
            holiday.setName(body.name().trim());
        }
        if (body.holidayDate() != null) {
            holiday.setHolidayDate(body.holidayDate());
        }
        if (body.paid() != null) {
            holiday.setPaid(body.paid());
        }
        return ResponseEntity.ok(ApiResponse.of("Holiday updated successfully", holidayRepository.save(holiday), HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('holidays:write')")
    @Operation(summary = "Delete a public holiday")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        PublicHoliday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));
        holidayRepository.delete(holiday);
        return ResponseEntity.ok(ApiResponse.of("Holiday deleted successfully", null, HttpStatus.OK));
    }

    public record HolidayBody(@NotBlank String name, @NotNull LocalDate holidayDate, Boolean paid) {
    }
}
