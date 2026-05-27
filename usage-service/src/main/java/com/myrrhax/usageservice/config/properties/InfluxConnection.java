package com.myrrhax.usageservice.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.influx")
public record InfluxConnection(
        String url,
        String token,
        String org,
        String bucket
) { }
