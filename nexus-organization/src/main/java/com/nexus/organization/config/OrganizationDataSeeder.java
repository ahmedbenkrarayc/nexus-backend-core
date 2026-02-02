package com.nexus.organization.config;

import com.nexus.organization.model.Organization;
import com.nexus.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrganizationDataSeeder implements ApplicationRunner {

    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        organizationRepository.findByCode(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE)
                .orElseGet(() -> organizationRepository.save(Organization.builder()
                        .name(OrganizationDefaults.DEFAULT_ORGANIZATION_NAME)
                        .code(OrganizationDefaults.DEFAULT_ORGANIZATION_CODE)
                        .active(true)
                        .build()));
    }
}