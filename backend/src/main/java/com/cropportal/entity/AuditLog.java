package com.cropportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_logs", indexes = @Index(name = "idx_audit_actor", columnList = "actor_id"))
public class AuditLog extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;
    private String userName;
    private String roleName;
    private String activity;
    private String module;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String ipAddress;
    private String device;
    private String browser;
    private String operatingSystem;
    private String loginStatus;
    private String status;
    private String remarks;
    @Column(columnDefinition = "TEXT")
    private String metadata;
}
