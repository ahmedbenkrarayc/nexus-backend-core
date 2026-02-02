package com.nexus.employee.repository;

import com.nexus.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long employeeId);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Employee> findByAuthUser(Long authUser);

    Page<Employee> findByCampusId(Long campusId, Pageable pageable);

    @Query("""
            select e
            from Employee e
            where (:campusId is null or e.campusId = :campusId)
          and (
            lower(e.code) like :searchPattern
            or lower(e.email) like :searchPattern
            or lower(e.fname) like :searchPattern
            or lower(e.lname) like :searchPattern
            or lower(concat(e.fname, ' ', e.lname)) like :searchPattern
          )
            """)
    Page<Employee> searchEmployees(
        @Param("campusId") Long campusId,
        @Param("searchPattern") String searchPattern,
        Pageable pageable);

    @Query("""
            select e
            from Employee e
            where (:campusId is null or e.campusId = :campusId)
          and (e.authUser in :userIds)
          and (
            lower(e.code) like :searchPattern
            or lower(e.email) like :searchPattern
            or lower(e.fname) like :searchPattern
            or lower(e.lname) like :searchPattern
            or lower(concat(e.fname, ' ', e.lname)) like :searchPattern
          )
            """)
    Page<Employee> searchEmployeesInAuthUsers(
        @Param("campusId") Long campusId,
        @Param("userIds") java.util.List<Long> userIds,
        @Param("searchPattern") String searchPattern,
        Pageable pageable);

    Page<Employee> findByCampusIdAndAuthUserIn(Long campusId, java.util.List<Long> userIds, Pageable pageable);

    Page<Employee> findByAuthUserIn(java.util.List<Long> userIds, Pageable pageable);
}