package com.kshrd.admsfileservice.employeemanage.model.dto.response;

import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceResponse {
    private UUID employeeId;
    private int year;
    private LeaveType type;
    private int entitled;
    private int used;
    private int pending;
    private int remaining;
}
