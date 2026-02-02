package com.nexus.organization.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Campus> campuses = new ArrayList<>();

    public void addCampus(Campus campus) {
        campuses.add(campus);
        campus.setOrganization(this);
    }

    public void removeCampus(Campus campus) {
        campuses.remove(campus);
        campus.setOrganization(null);
    }
}