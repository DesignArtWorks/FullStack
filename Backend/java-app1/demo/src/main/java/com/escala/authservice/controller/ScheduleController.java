package com.escala.authservice.controller;

import com.escala.authservice.dto.CreateShiftSwapRequest;
import com.escala.authservice.dto.DashboardSummaryResponse;
import com.escala.authservice.dto.DecideShiftSwapRequest;
import com.escala.authservice.dto.GenerateScheduleRequest;
import com.escala.authservice.dto.schedule.ShiftResponse;
import com.escala.authservice.dto.schedule.ShiftSwapResponse;
import com.escala.authservice.service.ScheduleService;
import com.escala.authservice.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final StatsService statsService;

    @GetMapping
    public List<ShiftResponse> listMonth(@RequestParam int year, @RequestParam int month, Authentication authentication) {
        return scheduleService.listMonth(year, month, authentication.getName()).stream()
                .map(ShiftResponse::from)
                .toList();
    }

    @PostMapping("/generate")
    public ResponseEntity<List<ShiftResponse>> generateMonth(@RequestBody GenerateScheduleRequest request, Authentication authentication) {
        return ResponseEntity.ok(scheduleService.generateMonth(request, authentication.getName()).stream()
                .map(ShiftResponse::from)
                .toList());
    }

    @GetMapping("/swap-requests")
    public List<ShiftSwapResponse> swapRequests(Authentication authentication) {
        return scheduleService.swapRequests(authentication.getName()).stream()
                .map(ShiftSwapResponse::from)
                .toList();
    }

    @PostMapping("/swap-requests")
    public ResponseEntity<ShiftSwapResponse> requestSwap(
            @RequestBody CreateShiftSwapRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ShiftSwapResponse.from(scheduleService.requestSwap(request, authentication.getName())));
    }

    @PatchMapping("/swap-requests/{id}/decision")
    public ResponseEntity<ShiftSwapResponse> decideSwap(@PathVariable UUID id, @RequestBody DecideShiftSwapRequest request, Authentication authentication) {
        return ResponseEntity.ok(ShiftSwapResponse.from(scheduleService.decideSwap(id, request, authentication.getName())));
    }

    @PatchMapping("/swap-requests/{id}/colleague-approval")
    public ResponseEntity<ShiftSwapResponse> approveByColleague(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(ShiftSwapResponse.from(scheduleService.approveByColleague(id, authentication.getName())));
    }

    @GetMapping("/dashboard-summary")
    public DashboardSummaryResponse dashboardSummary(@RequestParam int year, @RequestParam int month, Authentication authentication) {
        return statsService.getSummary(year, month, authentication.getName());
    }
}
