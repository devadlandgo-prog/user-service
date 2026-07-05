package com.landgo.userservice.repository;

import com.landgo.userservice.entity.PushTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PushTemplateRepository extends JpaRepository<PushTemplate, UUID> {
}
