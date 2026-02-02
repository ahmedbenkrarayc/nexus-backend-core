package com.nexus.organization.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country", nullable = false, length = 150)
    private String country;

    @Column(name = "city", nullable = false, length = 150)
    private String city;

    @Column(name = "lang", nullable = false)
    private double lang;

    @Column(name = "lat", nullable = false)
    private double lat;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;
}