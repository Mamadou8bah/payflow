package com.mamadou.sutura.support;

import com.mamadou.sutura.user.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SupportController {
    private final SupportTicketRepository tickets;
    public record CreateTicket(@NotBlank @Size(max = 160) String subject,
                               @NotBlank @Size(max = 5000) String message) {}
    public record Reply(@NotBlank @Size(max = 5000) String message, boolean resolved, long version) {}

    @GetMapping
    public List<SupportTicket> own(@AuthenticationPrincipal User user) {
        return tickets.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 100));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupportTicket create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateTicket input) {
        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(user.getId());
        ticket.setSubject(input.subject().trim());
        ticket.setMessage(input.message().trim());
        return tickets.save(ticket);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SupportTicket> queue(@RequestParam(defaultValue = "0") int page) {
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page");
        return tickets.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, 50));
    }

    @PatchMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SupportTicket reply(@PathVariable long id, @AuthenticationPrincipal User user,
                               @Valid @RequestBody Reply input) {
        SupportTicket ticket = tickets.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        if (ticket.getVersion() != input.version()) throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Ticket changed; refresh before replying");
        ticket.setStaffReply(input.message().trim());
        ticket.setStatus(input.resolved() ? "RESOLVED" : "OPEN");
        ticket.setRepliedBy(user.getId());
        ticket.setUpdatedAt(LocalDateTime.now());
        return tickets.saveAndFlush(ticket);
    }
}
