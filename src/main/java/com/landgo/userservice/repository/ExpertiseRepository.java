package com.landgo.userservice.repository;

import com.landgo.userservice.entity.Expertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpertiseRepository extends JpaRepository<Expertise, UUID> {
    List<Expertise> findByActiveTrue();
    boolean existsByName(String name);
}
