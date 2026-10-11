package com.mamadou.sutura.agent.controller;

import com.mamadou.sutura.agent.dto.AgentOperationResponse;
import com.mamadou.sutura.agent.service.AgentOperationService;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent/operations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
public class AgentController {

    private final AgentOperationService agentOperationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<java.util.List<AgentOperationResponse>> history(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(agentOperationService.history(currentUser));
    }

    public record CustomerSummary(long id, String name) {}

    @GetMapping("/customer")
    public ResponseEntity<CustomerSummary> customer(@RequestParam String phone) {
        if (!phone.matches("\\+?[0-9]{7,15}")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Enter a valid registered phone number");
        }
        User customer = userRepository.findByPhoneNumber(phone)
                .filter(user -> user.isEnabled() && user.isAccountNonLocked())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Customer not found"));
        return ResponseEntity.ok(new CustomerSummary(customer.getId(),
                customer.getFirstName() + " " + customer.getLastName()));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<AgentOperationResponse> lookup(
            @PathVariable String reference,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(agentOperationService.lookup(reference, currentUser));
    }

    @PostMapping("/{reference}/complete")
    public ResponseEntity<AgentOperationResponse> complete(
            @PathVariable String reference,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(agentOperationService.complete(reference, currentUser));
    }
}
