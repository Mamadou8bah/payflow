package com.mamadou.sutura.support;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
@Getter @Setter
public class SupportTicket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private long userId;
    @Column(nullable = false, length = 160) private String subject;
    @Column(nullable = false, length = 5000) private String message;
    @Column(nullable = false, length = 16) private String status = "OPEN";
    @Column(length = 5000) private String staffReply;
    private Long repliedBy;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();
    @Version private long version;
}
