package com.mamadou.sutura.support;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(long userId, Pageable page);
    List<SupportTicket> findAllByOrderByUpdatedAtDesc(Pageable page);
}
