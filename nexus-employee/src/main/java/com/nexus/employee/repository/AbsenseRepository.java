package com.nexus.employee.repository;

import com.nexus.employee.model.Absense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenseRepository extends JpaRepository<Absense, Long> {

    Optional<Absense> findByIdAndEmployeeId(Long absenseId, Long employeeId);

    Page<Absense> findByEmployeeId(Long employeeId, Pageable pageable);

    List<Absense> findByEmployeeId(Long employeeId);

    List<Absense> findByEmployeeIdIn(List<Long> employeeIds);

    Page<Absense> findAll(Pageable pageable);

    @Query("SELECT a FROM Absense a WHERE a.employee.campusId = :campusId ORDER BY a.id ASC")
    Page<Absense> findByCampusId(Long campusId, Pageable pageable);

        @Query("""
                        SELECT a
                        FROM Absense a
                        WHERE a.employee.id = :employeeId
                            AND a.start <= :requestedEnd
                            AND a.end >= :requestedStart
                        ORDER BY a.start ASC
                        """)
        List<Absense> findOverlappingAbsences(Long employeeId, LocalDate requestedStart, LocalDate requestedEnd);

    @Query("SELECT a FROM Absense a WHERE a.employee.id = :employeeId AND a.employee.campusId = :campusId")
    Page<Absense> findByEmployeeIdAndCampusId(Long employeeId, Long campusId, Pageable pageable);

    @Query("SELECT a FROM Absense a WHERE a.employee.campusId = :campusId AND " +
           "(LOWER(a.employee.fname) LIKE LOWER(CONCAT('%', :searchPattern, '%')) OR " +
           "LOWER(a.employee.lname) LIKE LOWER(CONCAT('%', :searchPattern, '%')) OR " +
           "LOWER(a.type) LIKE LOWER(CONCAT('%', :searchPattern, '%')))")
    Page<Absense> searchAbsensesByCampus(Long campusId, String searchPattern, Pageable pageable);
}
