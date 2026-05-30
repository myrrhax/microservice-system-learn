package com.myrrhax.alertservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private Long id;
    private Long userId;
    private Instant createdAt;
    private boolean sent;

    public Alert(Long userId, Instant createdAt, boolean sent) {
        this.userId = userId;
        this.createdAt = createdAt;
        this.sent = sent;
    }
}
