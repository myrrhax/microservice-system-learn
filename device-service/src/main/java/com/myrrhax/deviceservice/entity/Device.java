package com.myrrhax.deviceservice.entity;

import com.myrrhax.deviceservice.model.DeviceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "device_seq")
    @SequenceGenerator(
            name = "device_seq",
            sequenceName = "device_seq",
            allocationSize = 100
    )
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String location;

    @Column(name = "user_id")
    private Long userId;
}
