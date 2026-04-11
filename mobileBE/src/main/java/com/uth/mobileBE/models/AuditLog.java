package com.uth.mobileBE.models;

import com.uth.mobileBE.models.enums.ActionAuditLog;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ActionAuditLog action;

    private String entityName;

    @Column(columnDefinition = "TEXT")
    private String details; // JSON lưu thay đổi

    private Long createdAt;
}
