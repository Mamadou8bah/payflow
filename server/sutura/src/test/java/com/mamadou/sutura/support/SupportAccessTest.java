package com.mamadou.sutura.support;

import com.mamadou.sutura.user.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupportAccessTest {
    @Configuration @EnableMethodSecurity static class Config {
        @Bean SupportTicketRepository repository() { return mock(SupportTicketRepository.class); }
        @Bean SupportController controller(SupportTicketRepository repository) { return new SupportController(repository); }
    }
    private AnnotationConfigApplicationContext context;
    private SupportController controller;
    private SupportTicketRepository repository;
    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        controller = context.getBean(SupportController.class);
        repository = context.getBean(SupportTicketRepository.class);
        authenticate("USER");
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); context.close(); }
    void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "staff", "unused", List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
    @Test void customerCannotReadQueueOrReply() {
        assertThrows(AccessDeniedException.class, () -> controller.queue(0));
        assertThrows(AccessDeniedException.class, () -> controller.reply(1, User.builder().id(7).build(), new SupportController.Reply("Reply", true, 0)));
        verifyNoInteractions(repository);
    }
    @Test void ownTicketsAreAlwaysFilteredByAuthenticatedOwner() {
        controller.own(User.builder().id(7).build());
        verify(repository).findByUserIdOrderByCreatedAtDesc(eq(7L), any());
    }
    @Test void adminReplyRecordsStaffAndResolution() {
        authenticate("ADMIN");
        SupportTicket ticket = new SupportTicket();
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        controller.reply(1, User.builder().id(9).build(), new SupportController.Reply("Resolved", true, 0));
        assertEquals("RESOLVED", ticket.getStatus());
        assertEquals("Resolved", ticket.getStaffReply());
        assertEquals(9L, ticket.getRepliedBy());
        verify(repository).saveAndFlush(ticket);
    }
    @Test void staleReplyCannotOverwriteAnotherStaffResponse() {
        authenticate("ADMIN");
        SupportTicket ticket = new SupportTicket(); ticket.setVersion(2);
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        assertThrows(ResponseStatusException.class, () -> controller.reply(1, User.builder().id(9).build(), new SupportController.Reply("Stale", true, 1)));
        verify(repository, never()).saveAndFlush(any());
    }
}
