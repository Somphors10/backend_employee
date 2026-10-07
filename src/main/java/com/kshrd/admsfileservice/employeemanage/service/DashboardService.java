package com.kshrd.admsfileservice.employeemanage.service;

import com.kshrd.admsfileservice.employeemanage.model.dto.response.DashboardResponse;
import com.kshrd.admsfileservice.employeemanage.model.dto.response.NavigationItemResponse;

import java.util.List;

public interface DashboardService {
    DashboardResponse getDashboard();

    List<NavigationItemResponse> getNavigation();
}
