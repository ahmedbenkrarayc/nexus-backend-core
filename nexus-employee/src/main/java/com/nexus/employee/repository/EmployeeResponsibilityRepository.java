package com.nexus.employee.repository;

import com.nexus.employee.model.EmployeeResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeResponsibilityRepository extends JpaRepository<EmployeeResponsibility, Long> {

    Optional<EmployeeResponsibility> findFirstByEmployeeIdAndTypeAndActiveTrue(Long employeeId, String type);

    boolean existsByEmployeeIdAndTypeAndActiveTrue(Long employeeId, String type);

    boolean existsByResponsibleIdAndActiveTrue(Long responsibleId);

    List<EmployeeResponsibility> findAllByResponsibleIdAndTypeAndActiveTrueOrderByStartDateDesc(Long responsibleId, String type);
    
    @Query("SELECT er FROM EmployeeResponsibility er JOIN FETCH er.employee WHERE er.responsible.id = :responsibleId AND er.type = :type AND er.active = true ORDER BY er.startDate DESC")
    List<EmployeeResponsibility> findAllByResponsibleIdAndTypeAndActiveTrueWithEmployee(@Param("responsibleId") Long responsibleId, @Param("type") String type);
}