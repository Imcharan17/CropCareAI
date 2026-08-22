package com.cropportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ticket_messages", indexes = @Index(name = "idx_messages_ticket", columnList = "ticket_id"))
public class TicketMessage extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    private boolean readReceipt;
    private Instant readAt;
}
