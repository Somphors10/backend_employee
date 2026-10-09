package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.ReportOverviewResponse;

import java.time.LocalDate;

public interface ReportService {
    ReportOverviewResponse getOverview(LocalDate from, LocalDate to);

    StoredDocument exportCsv(String type, LocalDate from, LocalDate to);
}
