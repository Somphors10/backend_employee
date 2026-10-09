package com.kshrd.admsfileservice.employeemanage.security;

import com.kshrd.admsfileservice.employeemanage.exception.InvalidOperationException;
import com.kshrd.admsfileservice.employeemanage.model.entity.AppUser;
import com.kshrd.admsfileservice.employeemanage.model.entity.Employee;
import com.kshrd.admsfileservice.employeemanage.model.enums.Role;
import com.kshrd.admsfileservice.employeemanage.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final EmployeeRepository employeeRepository;

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AppUserDetails;
    }

    public AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            return null;
        }
        return details.getUser();
    }

    public Role currentRole() {
        AppUser user = currentUser();
        return user == null ? null : user.getRole();
    }

    public UUID currentEmployeeId() {
        AppUser user = currentUser();
        return user == null ? null : user.getEmployeeId();
    }

    public boolean canViewCompany() {
        if (!isAuthenticated()) {
            return true;
        }
        Role role = currentRole();
        return role == Role.ADMIN || role == Role.HR;
    }

    public boolean isHrOrAdmin() {
        Role role = currentRole();
        return role == Role.ADMIN || role == Role.HR;
    }

    public boolean canDecideFor(UUID employeeId) {
        if (employeeId == null || !isAuthenticated()) {
            return false;
        }
        if (isHrOrAdmin()) {
            return true;
        }
        if (currentRole() != Role.MANAGER) {
            return false;
        }
        UUID me = currentEmployeeId();
        if (me == null || me.equals(employeeId)) {
            return false;
        }
        return canViewEmployee(employeeId);
    }

    public void assertCanDecideFor(UUID employeeId) {
        if (!canDecideFor(employeeId)) {
            throw new AccessDeniedException("You cannot approve or reject this request");
        }
    }

    public void assertCanCorrectAttendance(UUID employeeId) {
        if (!isHrOrAdmin()) {
            throw new AccessDeniedException("Only HR or Admin can correct attendance");
        }
        assertCanViewEmployee(employeeId);
    }

    public Set<UUID> visibleEmployeeIds() {
        if (canViewCompany()) {
            return null;
        }
        UUID me = currentEmployeeId();
        if (me == null) {
            return Set.of();
        }
        Set<UUID> ids = new HashSet<>();
        ids.add(me);
        if (currentRole() == Role.MANAGER) {
            ids.addAll(employeeRepository.findByManagerId(me).stream()
                    .map(Employee::getId)
                    .collect(Collectors.toSet()));
        }
        return ids;
    }

    public boolean canViewEmployee(UUID employeeId) {
        if (employeeId == null) {
            return canViewCompany();
        }
        Set<UUID> ids = visibleEmployeeIds();
        return ids == null || ids.contains(employeeId);
    }

    public void assertCanViewEmployee(UUID employeeId) {
        if (!canViewEmployee(employeeId)) {
            throw new AccessDeniedException("You cannot access this employee record");
        }
    }

    public void assertCanActAsEmployee(UUID employeeId) {
        if (canViewCompany()) {
            return;
        }
        UUID me = currentEmployeeId();
        if (me == null || !me.equals(employeeId)) {
            throw new InvalidOperationException("You can only do this for your own employee profile");
        }
    }

    public UUID resolveEmployeeId(UUID requested) {
        if (requested != null) {
            assertCanViewEmployee(requested);
            return requested;
        }
        if (canViewCompany()) {
            return null;
        }
        return currentEmployeeId();
    }
}
