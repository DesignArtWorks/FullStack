package com.escala.authservice.controller;

import com.escala.authservice.dto.WorkPostRequest;
import com.escala.authservice.dto.WorkPostResponse;
import com.escala.authservice.service.WorkPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-posts")
@RequiredArgsConstructor
public class WorkPostController {
    private final WorkPostService workPostService;

    @GetMapping
    public List<WorkPostResponse> list(Authentication authentication) {
        return workPostService.list(authentication.getName()).stream().map(WorkPostResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<WorkPostResponse> create(Authentication authentication, @RequestBody WorkPostRequest request) {
        return ResponseEntity.ok(WorkPostResponse.from(workPostService.create(authentication.getName(), request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
        workPostService.delete(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }
}
