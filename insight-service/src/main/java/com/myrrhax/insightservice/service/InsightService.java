package com.myrrhax.insightservice.service;

import com.myrrhax.insightservice.client.UsageClient;
import com.myrrhax.insightservice.dto.DeviceDto;
import com.myrrhax.insightservice.dto.InsightDto;
import com.myrrhax.insightservice.dto.UsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {
    private final UsageClient usageClient;
    private final ChatModel chatModel;

    @Value("${app.overview-days:3}")
    private int overviewDays;

    public InsightDto getOverview(Long userId) {
        // Fetch data from usage service
        UsageDto usageData = usageClient.getXDaysUsageForUser(userId, overviewDays)
                .orElseThrow();

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();

        log.info("User {} total energy consumption is {}", userId, totalUsage);

        // Call gemini
        String prompt = new StringBuilder()
                .append("Analyse the following energy usage data and provide a ")
                .append("concise overview with actionable insights.")
                .append("This data is the aggregated data for the past ")
                .append(overviewDays)
                .append(" days.")
                .append("Usage Data: \n")
                .append(usageData.devices())
                .toString();

        ChatResponse response = chatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build()
        );

        return new InsightDto(userId, response.getResult().getOutput().getText(), totalUsage);
    }
}
