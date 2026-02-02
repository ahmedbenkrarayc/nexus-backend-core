package com.nexus.staffing.controller.employee;

import com.nexus.employee.dto.response.absense.AbsenseResponse;
import com.nexus.employee.dto.response.employeeskill.EmployeeSkillResponse;
import com.nexus.staffing.dto.response.employee.EmployeeAllocationRequestResponse;
import com.nexus.staffing.dto.response.employee.EmployeeEngagementResponse;
import com.nexus.staffing.dto.response.employee.EmployeeProjectDetailsResponse;
import com.nexus.staffing.service.EmployeeSelfServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees/me")
public class EmployeeSelfServiceController {

    private final EmployeeSelfServiceService employeeSelfService;

    @GetMapping("/engagements")
    public List<EmployeeEngagementResponse> getMyEngagements() {
        return employeeSelfService.getMyEngagements();
    }

    @GetMapping("/projects/{projectId}")
    public EmployeeProjectDetailsResponse getMyProjectDetails(@PathVariable Long projectId) {
        return employeeSelfService.getMyProjectDetails(projectId);
    }

    @GetMapping("/requests")
    public List<EmployeeAllocationRequestResponse> getMyAllocationRequests() {
        return employeeSelfService.getMyAllocationRequests();
    }

    @GetMapping("/requests/{requestId}")
    public EmployeeAllocationRequestResponse getMyAllocationRequestDetails(@PathVariable Long requestId) {
        return employeeSelfService.getMyAllocationRequestDetails(requestId);
    }

    @GetMapping("/skills")
    public List<EmployeeSkillResponse> getMySkills() {
        return employeeSelfService.getMySkills();
    }

    @GetMapping("/absences")
    public List<AbsenseResponse> getMyAbsences() {
        return employeeSelfService.getMyAbsences();
    }
}
