package com.escala.authservice.controller;

import com.escala.authservice.dto.OperationalCapacityRequest;
import com.escala.authservice.dto.schedule.OperationalCapacityResponse;
import com.escala.authservice.service.OperationalCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operational-capacities")
@RequiredArgsConstructor
public class OperationalCapacityController {

    private final OperationalCapacityService operationalCapacityService;

    @GetMapping
    public ResponseEntity<List<OperationalCapacityResponse>> listCapacities(Authentication authentication) {
        return ResponseEntity.ok(operationalCapacityService.listCapacities(authentication.getName()).stream()
                .map(OperationalCapacityResponse::from)
                .toList());
    }

    @GetMapping("/target")
    public ResponseEntity<List<OperationalCapacityResponse>> listByTarget(
            Authentication authentication,
            @RequestParam UUID targetId,
            @RequestParam String targetType
    ) {
        return ResponseEntity.ok(operationalCapacityService.listByTarget(authentication.getName(), targetId, targetType).stream()
                .map(OperationalCapacityResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<OperationalCapacityResponse> createCapacity(
            Authentication authentication,
            @RequestBody OperationalCapacityRequest request
    ) {
        return ResponseEntity.ok(OperationalCapacityResponse.from(
                operationalCapacityService.createCapacity(authentication.getName(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperationalCapacityResponse> updateCapacity(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody OperationalCapacityRequest request
    ) {
        return ResponseEntity.ok(OperationalCapacityResponse.from(
                operationalCapacityService.updateCapacity(authentication.getName(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapacity(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        operationalCapacityService.deleteCapacity(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
