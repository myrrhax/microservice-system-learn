package com.myrrhax.alertservice.repository;

import com.myrrhax.alertservice.entity.Alert;

import java.util.Optional;

public interface AlertRepository {
    Optional<Alert> findById(Long id);
    Alert save(Alert alert);
}
