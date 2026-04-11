package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.Role;
import com.uth.mobileBE.models.enums.StatusLibrary;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(unique = true, nullable = false)
    private String username;

    private String passwordHash;
    private String fullname;

    private Role role;

    private Boolean isActive;
}
