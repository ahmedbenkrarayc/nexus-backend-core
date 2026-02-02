package com.nexus.staffing.service.impl;

import com.nexus.employee.client.AuthServiceClient;
import com.nexus.employee.model.Absense;
import com.nexus.employee.model.Employee;
import com.nexus.employee.model.EmployeeSkill;
import com.nexus.employee.repository.AbsenseRepository;
import com.nexus.employee.repository.EmployeeRepository;
import com.nexus.employee.repository.EmployeeSkillRepository;
import com.nexus.organization.model.Campus;
import com.nexus.organization.model.Location;
import com.nexus.organization.repository.CampusRepository;
import com.nexus.project.model.Project;
import com.nexus.project.repository.ProjectRepository;
import com.nexus.shared.security.context.CurrentUserContext;
import com.nexus.shared.security.provider.CurrentUserProvider;
import com.nexus.staffing.dto.request.candidatesearch.SearchCandidatesRequest;
import com.nexus.staffing.dto.response.candidatesearch.CandidateMatchResponse;
import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.enums.LocationPriorityCategory;
import com.nexus.staffing.repository.EngagementRepository;
import com.nexus.staffing.service.CandidateMatchingService;
import com.nexus.staffing.util.EmployeeAvailabilityEvaluator;
import com.nexus.staffing.util.EmployeeScoringEngine;
import com.nexus.staffing.util.EmployeeScoringResult;
import com.nexus.staffing.util.LocationPriorityEvaluator;
import com.nexus.staffing.util.SkillMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for candidate matching.
 * Orchestrates the evaluation of candidates against search criteria.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateMatchingServiceImpl implements CandidateMatchingService {

    private static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String ROLE_RESOURCE_MANAGER = "RESOURCE_MANAGER";
    private static final String ROLE_HR_MANAGER = "HR_MANAGER";

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EngagementRepository engagementRepository;
    private final AbsenseRepository absenseRepository;
    private final CampusRepository campusRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AuthServiceClient authServiceClient;
    private final LocationPriorityEvaluator locationEvaluator;
    private final EmployeeAvailabilityEvaluator availabilityEvaluator;
    private final SkillMatcher skillMatcher;
    private final EmployeeScoringEngine scoringEngine;

    @Override
    public List<CandidateMatchResponse> searchCandidates(SearchCandidatesRequest request) {
        // 1. Validate current user is PM and owns the project
        CurrentUserContext caller = currentUserProvider.getCurrentUser();
        requireRole(caller, "Only project managers can search candidates", ROLE_PROJECT_MANAGER);
        
        Project project = projectRepository.findByIdAndSponsorEmployeeId(
            request.projectId(),
            caller.userId()
        ).orElseThrow(() -> notFound("Project not found or you don't own it"));

        // 2. Validate date range
        if (request.requestedEndDate().isBefore(request.requestedStartDate())) {
            throw badRequest("Requested end date cannot be before start date");
        }

        // 3. Load project's campus with location
        Campus projectCampus = campusRepository.findById(project.getOwnerCampusId())
            .orElseThrow(() -> new RuntimeException("Project campus not found"));

        // 4. Load only employees with 'EMPLOYEE' role, excluding managers
        List<Long> employeeUserIds = new ArrayList<>(authServiceClient.getUserIdsByRole(ROLE_EMPLOYEE));
        if (employeeUserIds.isEmpty()) {
            return List.of();
        }

        // Exclude specific manager roles
        List<Long> pmIds = authServiceClient.getUserIdsByRole(ROLE_PROJECT_MANAGER);
        List<Long> rmIds = authServiceClient.getUserIdsByRole(ROLE_RESOURCE_MANAGER);
        List<Long> hrIds = authServiceClient.getUserIdsByRole(ROLE_HR_MANAGER);
        
        employeeUserIds.removeAll(pmIds);
        employeeUserIds.removeAll(rmIds);
        employeeUserIds.removeAll(hrIds);

        if (employeeUserIds.isEmpty()) {
            return List.of();
        }

        List<Employee> allEmployees = employeeRepository.findByAuthUserIn(employeeUserIds, org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (allEmployees.isEmpty()) {
            return List.of();
        }

        // 5. Load all skills for these employees
        List<Long> employeeIds = allEmployees.stream().map(Employee::getId).toList();
        List<EmployeeSkill> allEmployeeSkills = employeeSkillRepository.findAll();
        Map<Long, List<EmployeeSkill>> skillsByEmployeeId = allEmployeeSkills.stream()
            .collect(Collectors.groupingBy(es -> es.getEmployee().getId()));

        // 6. Load all engagements for these employees
        List<Engagement> allEngagements = engagementRepository.findByEmployeeIdIn(employeeIds);
        Map<Long, List<Engagement>> engagementsByEmployeeId = allEngagements.stream()
            .collect(Collectors.groupingBy(Engagement::getEmployeeId));

        // 7. Load all absences for these employees
        List<Absense> allAbsences = absenseRepository.findByEmployeeIdIn(employeeIds);
        Map<Long, List<Absense>> absencesByEmployeeId = allAbsences.stream()
            .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        // 8. Score and rank each candidate
        List<CandidateMatchResponse> candidates = allEmployees.stream()
            .map(employee -> scoreCandidateEmployee(
                employee,
                projectCampus,
                request,
                skillsByEmployeeId.getOrDefault(employee.getId(), List.of()),
                engagementsByEmployeeId.getOrDefault(employee.getId(), List.of()),
                absencesByEmployeeId.getOrDefault(employee.getId(), List.of())
            ))
            .filter(Objects::nonNull)
            .sorted(Comparator
                .comparingInt(CandidateMatchResponse::matchPercentage).reversed()  // Highest score first
                .thenComparing(CandidateMatchResponse::availableForRequestedPeriod, Comparator.reverseOrder())  // Available first
                .thenComparing(CandidateMatchResponse::locationPriorityCategory, 
                    (a, b) -> {
                        // SAME_CAMPUS > SAME_COUNTRY > DIFFERENT_COUNTRY
                        Integer aOrder = switch(a) {
                            case SAME_CAMPUS -> 0;
                            case SAME_COUNTRY -> 1;
                            case DIFFERENT_COUNTRY -> 2;
                        };
                        Integer bOrder = switch(b) {
                            case SAME_CAMPUS -> 0;
                            case SAME_COUNTRY -> 1;
                            case DIFFERENT_COUNTRY -> 2;
                        };
                        return aOrder.compareTo(bOrder);
                    }
                )
            )
            .limit(request.getMaxResults())
            .toList();

        return candidates;
    }

    /**
     * Scores a single candidate employee against the search criteria.
     */
    private CandidateMatchResponse scoreCandidateEmployee(
            Employee employee,
            Campus projectCampus,
            SearchCandidatesRequest request,
            List<EmployeeSkill> employeeSkills,
            List<Engagement> employeeEngagements,
            List<Absense> employeeAbsences
    ) {
        // Get employee's campus with location info
        Campus employeeCampus = campusRepository.findById(employee.getCampusId())
            .orElse(null);

        // Evaluate location priority
        LocationPriorityCategory locationCategory = locationEvaluator.evaluatePriority(
            projectCampus,
            employeeCampus
        );

        // Match skills
        List<String> matchedRequired = skillMatcher.getMatchedRequiredSkills(
            employeeSkills,
            request.requiredSkills()
        );
        List<String> missingRequired = skillMatcher.getMissingRequiredSkills(
            employeeSkills,
            request.requiredSkills()
        );
        List<String> matchedNiceToHave = skillMatcher.getMatchedNiceToHaveSkills(
            employeeSkills,
            request.niceToHaveSkills()
        );
        List<String> skillsBelowLevel = skillMatcher.getSkillsNotMeetingMinimumLevel(
            employeeSkills,
            request.minimumSkillLevels()
        );

        // Evaluate availability
        boolean hasAbsenceConflict = availabilityEvaluator.hasAbsenceConflict(
            employeeAbsences,
            request.requestedStartDate(),
            request.requestedEndDate()
        );
        int overlappingCount = availabilityEvaluator.countOverlappingEngagements(
            employeeEngagements,
            request.requestedStartDate(),
            request.requestedEndDate()
        );
        boolean isAvailable = availabilityEvaluator.isAvailable(
            employeeAbsences,
            employeeEngagements,
            request.requestedStartDate(),
            request.requestedEndDate()
        );

        // Calculate scores
        int locationScore = locationEvaluator.scoreLocationPriority(locationCategory);
        int skillLevelPenalty = Math.min(15, skillsBelowLevel.size() * 3);  // Penalty per skill below level
        int availabilityScore = isAvailable ? 15 : 8;  // Reduced if not available

        EmployeeScoringResult scoring = scoringEngine.calculateScore(
            matchedRequired.size(),
            request.requiredSkills().size(),
            matchedNiceToHave.size(),
            request.niceToHaveSkills() != null ? request.niceToHaveSkills().size() : 0,
            skillLevelPenalty,
            locationScore,
            availabilityScore,
            hasAbsenceConflict,
            overlappingCount
        );

        // Build explanation
        List<String> explanations = buildExplanations(
            employee,
            projectCampus,
            employeeCampus,
            locationCategory,
            matchedRequired,
            missingRequired,
            matchedNiceToHave,
            skillsBelowLevel,
            hasAbsenceConflict,
            overlappingCount,
            isAvailable
        );

        Location employeeLocation = employeeCampus != null ? employeeCampus.getLocation() : null;

        return new CandidateMatchResponse(
            employee.getId(),
            employee.getFname(),
            employee.getLname(),
            employeeCampus != null ? employeeCampus.getName() : "Unknown",
            employeeLocation != null ? employeeLocation.getCountry() : "Unknown",
            isAvailable,
            !hasAbsenceConflict && overlappingCount <= 1,
            scoring.percentage(),
            locationCategory,
            matchedRequired,
            missingRequired,
            matchedNiceToHave,
            overlappingCount,
            hasAbsenceConflict,
            explanations,
            scoring.breakdown()
        );
    }

    /**
     * Builds human-readable explanation for the candidate score.
     */
    private List<String> buildExplanations(
            Employee employee,
            Campus projectCampus,
            Campus employeeCampus,
            LocationPriorityCategory locationCategory,
            List<String> matchedRequired,
            List<String> missingRequired,
            List<String> matchedNiceToHave,
            List<String> skillsBelowLevel,
            boolean hasAbsenceConflict,
            int overlappingCount,
            boolean isAvailable
    ) {
        List<String> explanations = new ArrayList<>();

        // Location
        switch (locationCategory) {
            case SAME_CAMPUS -> explanations.add(
                "Same campus as project (" + (projectCampus != null ? projectCampus.getName() : "Unknown") + ")"
            );
            case SAME_COUNTRY -> explanations.add(
                "Different campus but same country (employee: " + 
                (employeeCampus != null ? employeeCampus.getName() : "Unknown") + ")"
            );
            case DIFFERENT_COUNTRY -> explanations.add("Different country");
        }

        // Skills
        if (matchedRequired.isEmpty()) {
            explanations.add("No required skills matched");
        } else {
            explanations.add("Matches " + matchedRequired.size() + " required skill(s): " + 
                String.join(", ", matchedRequired));
        }

        if (!missingRequired.isEmpty()) {
            explanations.add("Missing " + missingRequired.size() + " required skill(s): " +
                String.join(", ", missingRequired));
        }

        if (!matchedNiceToHave.isEmpty()) {
            explanations.add("Matches " + matchedNiceToHave.size() + " nice-to-have skill(s): " +
                String.join(", ", matchedNiceToHave));
        }

        if (!skillsBelowLevel.isEmpty()) {
            explanations.add("Does not meet minimum level for: " + String.join(", ", skillsBelowLevel));
        }

        // Availability
        if (hasAbsenceConflict) {
            explanations.add("⚠ Has approved absence conflict in requested period");
        }

        if (overlappingCount == 0) {
            explanations.add("No overlapping engagements in requested period");
        } else if (overlappingCount == 1) {
            explanations.add("1 overlapping engagement in requested period");
        } else {
            explanations.add(overlappingCount + " overlapping engagements in requested period");
        }

        return explanations;
    }

    /**
     * Validates that current user has PROJECT_MANAGER role.
     */
    private void requireRole(CurrentUserContext caller, String message, String... roles) {
        if (caller == null || !caller.hasAnyRole(roles)) {
            throw forbidden(message);
        }
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
