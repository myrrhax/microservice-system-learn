package com.myrrhax.insightservice.controller;

import com.myrrhax.insightservice.dto.InsightDto;
import com.myrrhax.insightservice.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/insight")
@RequiredArgsConstructor
public class InsightController {
    private final InsightService insightService;

//    @GetMapping("saving-tips/{userId:\\d+}")
//    public ResponseEntity<InsightDto> getSavingTips(@PathVariable Long userId) {
//        InsightDto insight = insightService.getSavingTips(userId);
//
//        return ResponseEntity.ok(insight);
//    }

    @GetMapping("overview/{userId:\\d+}")
    public ResponseEntity<InsightDto> getOverview(@PathVariable Long userId) {
        InsightDto insight = insightService.getOverview(userId);

        return ResponseEntity.ok(insight);
    }
}
