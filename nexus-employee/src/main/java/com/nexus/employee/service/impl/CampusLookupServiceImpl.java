package com.nexus.employee.service.impl;

import com.nexus.employee.exception.ResourceNotFoundException;
import com.nexus.employee.service.CampusLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampusLookupServiceImpl implements CampusLookupService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void ensureCampusExists(Long campusId) {
        Long matches = jdbcTemplate.queryForObject(
                "select count(*) from campus where id = ?",
                Long.class,
                campusId
        );

        if (matches == null || matches == 0L) {
            throw new ResourceNotFoundException("Campus not found with id " + campusId);
        }
    }
}
