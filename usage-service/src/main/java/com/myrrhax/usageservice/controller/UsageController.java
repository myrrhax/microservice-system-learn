package com.myrrhax.usageservice.controller;

import com.myrrhax.usageservice.dto.UsageDto;
import com.myrrhax.usageservice.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/usage")
public class UsageController {
    private final UsageService usageService;

    @GetMapping("{userId:\\d+}")
    public ResponseEntity<UsageDto> getUserDevicesUsage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int days
    ) {
        UsageDto usage = usageService.getXDaysUsageForUser(userId, days);

        return ResponseEntity.ok(usage);
    }
}
