package com.nexus.employee.controller;

import com.nexus.employee.dto.request.absense.CreateAbsenseRequest;
import com.nexus.employee.dto.request.absense.UpdateAbsenseRequest;
import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.dto.response.absense.AbsensePageResponse;
import com.nexus.employee.service.AbsenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/absences")
public class AbsenseController {

    private final AbsenseService absenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbsenseResponse createAbsense(@Valid @RequestBody CreateAbsenseRequest request) {
        return absenseService.createAbsense(request);
    }

    @GetMapping("/{absenseId}")
    public AbsenseResponse getAbsenseDetails(@PathVariable Long absenseId) {
        return absenseService.getAbsenseDetails(absenseId);
    }

    @PutMapping("/{absenseId}")
    public AbsenseResponse updateAbsense(
            @PathVariable Long absenseId,
            @Valid @RequestBody UpdateAbsenseRequest request) {
        return absenseService.updateAbsense(absenseId, request);
    }

    @DeleteMapping("/{absenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAbsense(@PathVariable Long absenseId) {
        absenseService.deleteAbsense(absenseId);
    }

    @GetMapping("/employee/{employeeId}")
    public AbsensePageResponse listAbsensesByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return absenseService.listAbsensesByEmployee(employeeId, page, size);
    }

    @GetMapping
    public AbsensePageResponse listAllAbsenses(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return absenseService.listAllAbsenses(page, size, search);
    }
}
