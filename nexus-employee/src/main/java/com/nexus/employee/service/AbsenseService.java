package com.nexus.employee.service;

import com.nexus.employee.dto.request.absense.CreateAbsenseRequest;
import com.nexus.employee.dto.request.absense.UpdateAbsenseRequest;
import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.dto.response.absense.AbsensePageResponse;

public interface AbsenseService {

    AbsenseResponse createAbsense(CreateAbsenseRequest request);

    AbsenseResponse getAbsenseDetails(Long absenseId);

    AbsenseResponse updateAbsense(Long absenseId, UpdateAbsenseRequest request);

    void deleteAbsense(Long absenseId);

    AbsensePageResponse listAbsensesByEmployee(Long employeeId, int page, int size);

    AbsensePageResponse listAllAbsenses(int page, int size, String search);
}
