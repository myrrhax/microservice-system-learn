package com.myrrhax.insightservice.service;

import com.myrrhax.insightservice.client.UsageClient;
import com.myrrhax.insightservice.dto.InsightDto;
import com.myrrhax.insightservice.dto.UsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {
    private final UsageClient usageClient;

    @Value("${app.overview-days:3}")
    private int overviewDays;

    public InsightDto getOverview(Long userId) {
        // Fetch data from usage service
        UsageDto usageData = usageClient.getXDaysUsageForUser(userId, overviewDays)
                .orElseThrow();

        return null;
    }
}
