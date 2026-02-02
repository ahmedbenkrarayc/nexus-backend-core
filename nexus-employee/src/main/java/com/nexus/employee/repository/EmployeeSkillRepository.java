package com.nexus.employee.repository;

import com.nexus.employee.model.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {

    List<EmployeeSkill> findAllByEmployeeIdOrderByIdAsc(Long employeeId);
    
    List<EmployeeSkill> findAllByEmployeeIdIn(List<Long> employeeIds);

    Optional<EmployeeSkill> findByIdAndEmployeeId(Long id, Long employeeId);

    boolean existsByEmployeeIdAndSkillId(Long employeeId, Long skillId);
}
