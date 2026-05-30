package com.myrrhax.alertservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private Long id;
    private Long userId;
    private OffsetDateTime createdAt;
    private boolean sent;

    public Alert(Long userId, OffsetDateTime createdAt, boolean sent) {
        this.userId = userId;
        this.createdAt = createdAt;
        this.sent = sent;
    }
}
