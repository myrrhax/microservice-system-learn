package com.myrrhax.ingestionservice.config;

import com.myrrhax.ingestionservice.event.EnergyUsageEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaBeans {
    @Bean
    public NewTopic energyUsageTopic() {
        return TopicBuilder
                .name("energy-usage")
                .replicas(1)
                .partitions(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, EnergyUsageEvent> producerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.RETRIES_CONFIG, 10);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
        config.put(JacksonJsonSerializer.TYPE_MAPPINGS,
                "energy-duration:com.myrrhax.ingestionservice.event.EnergyUsageEvent");

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new JacksonJsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, EnergyUsageEvent> kafkaTemplate(
            ProducerFactory<String, EnergyUsageEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
