package com.nexus.organization.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @OneToOne(mappedBy = "campus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Location location;

    public void setLocation(Location location) {
        this.location = location;
        if (location != null) {
            location.setCampus(this);
        }
    }
}