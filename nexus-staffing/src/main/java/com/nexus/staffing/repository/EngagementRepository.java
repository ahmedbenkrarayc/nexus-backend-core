package com.nexus.staffing.repository;

import com.nexus.staffing.model.Engagement;
import com.nexus.staffing.model.enums.EngagementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EngagementRepository extends JpaRepository<Engagement, Long> {

    List<Engagement> findByAllocationRequestIdIn(List<Long> allocationRequestIds);

        List<Engagement> findByAllocationRequestId(Long allocationRequestId);

    List<Engagement> findByEmployeeId(Long employeeId);

    List<Engagement> findByEmployeeIdIn(List<Long> employeeIds);

        List<Engagement> findByEmployeeIdInAndStatusIn(List<Long> employeeIds, List<EngagementStatus> statuses);

        @Query("""
                        select e
                        from Engagement e
                        where e.employeeId = :employeeId
                            and e.status in :statuses
                            and e.startDate <= :requestedEnd
                            and (e.endDate is null or e.endDate >= :requestedStart)
                        """)
        List<Engagement> findOverlappingEngagements(
                        @Param("employeeId") Long employeeId,
                        @Param("requestedStart") LocalDate requestedStart,
                        @Param("requestedEnd") LocalDate requestedEnd,
                        @Param("statuses") List<EngagementStatus> statuses);
}
