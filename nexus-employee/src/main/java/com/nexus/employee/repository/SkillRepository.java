package com.nexus.employee.repository;

import com.nexus.employee.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findAllByOrderByIdAsc();

    List<Skill> findAllByActiveTrueOrderByIdAsc();

    boolean existsByNameIgnoreCaseAndCategoryIgnoreCase(String name, String category);

    boolean existsByNameIgnoreCaseAndCategoryIgnoreCaseAndIdNot(String name, String category, Long id);
}