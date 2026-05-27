package com.myrrhax.usageservice.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.myrrhax.usageservice.config.properties.InfluxConnection;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({InfluxConnection.class})
public class InfluxDbBeans {
    @Bean
    public InfluxDBClient influxDBClient(InfluxConnection influxConnection) {
        return InfluxDBClientFactory.create(
                influxConnection.url(),
                influxConnection.token().toCharArray(),
                influxConnection.org()
        );
    }
}
